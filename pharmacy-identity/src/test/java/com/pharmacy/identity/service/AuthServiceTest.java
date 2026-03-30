package com.pharmacy.identity.service;

import com.pharmacy.common.enums.Role;
import com.pharmacy.common.enums.UserStatus;
import com.pharmacy.common.events.UserLoggedInEvent;
import com.pharmacy.common.events.UserRegisteredEvent;
import com.pharmacy.common.exception.BadRequestException;
import com.pharmacy.common.exception.ResourceNotFoundException;
import com.pharmacy.common.exception.UnauthorizedException;
import com.pharmacy.common.util.JwtUtil;
import com.pharmacy.identity.dto.AuthResponse;
import com.pharmacy.identity.dto.LoginRequest;
import com.pharmacy.identity.dto.SignupRequest;
import com.pharmacy.identity.dto.UserDTO;
import com.pharmacy.identity.entity.User;
import com.pharmacy.identity.event.IdentityEventPublisher;
import com.pharmacy.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtBlacklistService jwtBlacklistService;

    @Mock
    private IdentityEventPublisher identityEventPublisher;

    @InjectMocks
    private AuthService authService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(10L)
                .name("Jane Doe")
                .email("jane@test.com")
                .passwordHash("hash")
                .mobile("555")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void signup_throwsWhenEmailExists() {
        when(userRepository.existsByEmail("x@test.com")).thenReturn(true);

        SignupRequest request = SignupRequest.builder().name("N").email("x@test.com").password("secret1").build();
        assertThrows(BadRequestException.class, () -> authService.signup(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_savesUser_publishesEvent_firstNameFromFullName() {
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secret1")).thenReturn("encoded");
        User saved = User.builder()
                .id(42L)
                .name("John Smith")
                .email("new@test.com")
                .passwordHash("encoded")
                .mobile("1")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserDTO dto = authService.signup(SignupRequest.builder()
                .name("John Smith")
                .email("new@test.com")
                .password("secret1")
                .mobile("1")
                .build());

        assertEquals(42L, dto.getId());
        assertEquals("new@test.com", dto.getEmail());
        verify(passwordEncoder).encode("secret1");

        ArgumentCaptor<UserRegisteredEvent> cap = ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(identityEventPublisher).publishUserRegistered(cap.capture());
        assertEquals(42L, cap.getValue().getUserId());
        assertEquals("John", cap.getValue().getFirstName());
    }

    @Test
    void signup_publishFailureStillReturnsUser() {
        when(userRepository.existsByEmail("a@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("h");
        User saved = User.builder()
                .id(7L)
                .name("Solo")
                .email("a@test.com")
                .passwordHash("h")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.save(any())).thenReturn(saved);
        doThrow(new RuntimeException("broker down")).when(identityEventPublisher).publishUserRegistered(any());

        UserDTO dto = authService.signup(SignupRequest.builder()
                .name("Solo")
                .email("a@test.com")
                .password("secret1")
                .build());

        assertEquals(7L, dto.getId());
        verify(identityEventPublisher).publishUserRegistered(any());
    }

    @Test
    void signup_blankName_mapsFirstNameToThere() {
        when(userRepository.existsByEmail("b@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("h");
        User saved = User.builder()
                .id(8L)
                .name("  ")
                .email("b@test.com")
                .passwordHash("h")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.save(any())).thenReturn(saved);

        authService.signup(SignupRequest.builder()
                .name("  ")
                .email("b@test.com")
                .password("secret1")
                .build());

        ArgumentCaptor<UserRegisteredEvent> cap = ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(identityEventPublisher).publishUserRegistered(cap.capture());
        assertEquals("there", cap.getValue().getFirstName());
    }

    @Test
    void login_success() {
        LoginRequest req = LoginRequest.builder().email("jane@test.com").password("p").build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("jane@test.com")).thenReturn(Optional.of(activeUser));
        when(jwtUtil.generateToken(10L, "jane@test.com", "CUSTOMER")).thenReturn("jwt-token");

        AuthResponse res = authService.login(req);

        assertEquals("jwt-token", res.getToken());
        assertEquals("Bearer", res.getTokenType());
        assertEquals(10L, res.getUserId());
        assertEquals("jane@test.com", res.getEmail());
        assertEquals("CUSTOMER", res.getRole());

        ArgumentCaptor<UserLoggedInEvent> loginCap = ArgumentCaptor.forClass(UserLoggedInEvent.class);
        verify(identityEventPublisher).publishUserLoggedIn(loginCap.capture());
        assertEquals(10L, loginCap.getValue().getUserId());
        assertEquals("jane@test.com", loginCap.getValue().getEmail());
        assertEquals("Jane", loginCap.getValue().getFirstName());
    }

    @Test
    void login_publishLoggedInFailureStillReturnsToken() {
        doThrow(new RuntimeException("rabbit down")).when(identityEventPublisher).publishUserLoggedIn(any());

        LoginRequest req = LoginRequest.builder().email("jane@test.com").password("p").build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("jane@test.com")).thenReturn(Optional.of(activeUser));
        when(jwtUtil.generateToken(10L, "jane@test.com", "CUSTOMER")).thenReturn("jwt-token");

        AuthResponse res = authService.login(req);
        assertEquals("jwt-token", res.getToken());
        verify(identityEventPublisher).publishUserLoggedIn(any());
    }

    @Test
    void login_badCredentials_wrapped() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        LoginRequest request = LoginRequest.builder().email("x@test.com").password("y").build();
        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(identityEventPublisher, never()).publishUserLoggedIn(any());
    }

    @Test
    void login_authThrowsGeneric_wrappedAsBadCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new RuntimeException("ldap down"));

        LoginRequest request = LoginRequest.builder().email("x@test.com").password("y").build();
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_userMissingAfterAuth() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        LoginRequest request = LoginRequest.builder().email("ghost@test.com").password("p").build();
        assertThrows(ResourceNotFoundException.class, () -> authService.login(request));

        verify(identityEventPublisher, never()).publishUserLoggedIn(any());
    }

    @Test
    void login_inactiveAccount() {
        User inactive = User.builder()
                .id(1L)
                .name("I")
                .email("i@test.com")
                .passwordHash("h")
                .role(Role.CUSTOMER)
                .status(UserStatus.INACTIVE)
                .build();
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("i@test.com")).thenReturn(Optional.of(inactive));

        LoginRequest request = LoginRequest.builder().email("i@test.com").password("p").build();
        assertThrows(UnauthorizedException.class, () -> authService.login(request));

        verify(identityEventPublisher, never()).publishUserLoggedIn(any());
    }

    @Test
    void logout_stripsBearerAndBlacklists() {
        when(jwtUtil.getExpirationTime("raw")).thenReturn(999L);

        authService.logout("Bearer raw");

        verify(jwtBlacklistService).blacklistToken("raw", 999L);
    }

    @Test
    void logout_rawTokenWithoutPrefix() {
        when(jwtUtil.getExpirationTime("tok")).thenReturn(1L);

        authService.logout("tok");

        verify(jwtBlacklistService).blacklistToken("tok", 1L);
    }

    @Test
    void validateToken_falseWhenBlacklisted() {
        when(jwtBlacklistService.isTokenBlacklisted("t")).thenReturn(true);

        assertFalse(authService.validateToken("t"));
        verify(jwtUtil, never()).validateToken(any());
    }

    @Test
    void validateToken_stripsBearer_delegatesToJwtUtil() {
        when(jwtBlacklistService.isTokenBlacklisted("inner")).thenReturn(false);
        when(jwtUtil.validateToken("inner")).thenReturn(true);

        assertTrue(authService.validateToken("Bearer inner"));
    }

    @Test
    void getUserById_found() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(activeUser));

        UserDTO dto = authService.getUserById(10L);

        assertEquals("jane@test.com", dto.getEmail());
        assertEquals("CUSTOMER", dto.getRole());
    }

    @Test
    void getUserById_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.getUserById(99L));
    }

    @Test
    void getUserByEmail_found() {
        when(userRepository.findByEmail("jane@test.com")).thenReturn(Optional.of(activeUser));

        UserDTO dto = authService.getUserByEmail("jane@test.com");

        assertEquals(10L, dto.getId());
    }

    @Test
    void getUserByEmail_notFound() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.getUserByEmail("missing@test.com"));
    }
}
