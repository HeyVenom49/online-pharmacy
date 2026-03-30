package com.pharmacy.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.common.dto.notification.CreateNotificationDispatchRequest;
import com.pharmacy.identity.dto.NotificationDTO;
import com.pharmacy.identity.entity.Notification.NotificationType;
import com.pharmacy.identity.entity.User;
import com.pharmacy.identity.repository.UserRepository;
import com.pharmacy.identity.service.AuthService;
import com.pharmacy.identity.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Objects;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InternalIdentityControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new InternalIdentityController(authService, notificationService, userRepository))
                .build();
    }

    @Test
    void validateToken_returnsValidity() throws Exception {
        when(authService.validateToken("abc")).thenReturn(true);

        mockMvc.perform(get("/internal/token/validate").param("token", "abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void dispatchNotification_returnsIdAndEmail() throws Exception {
        CreateNotificationDispatchRequest body = CreateNotificationDispatchRequest.builder()
                .userId(1L)
                .type("ORDER_PLACED")
                .title("Hello")
                .message("Body")
                .referenceId("ref-1")
                .build();

        NotificationDTO dto = NotificationDTO.builder().id(50L).build();
        when(notificationService.createNotification(
                1L, NotificationType.ORDER_PLACED, "Hello", "Body", "ref-1"))
                .thenReturn(dto);

        User user = User.builder()
                .id(1L)
                .name("N")
                .email("user@test.com")
                .passwordHash("h")
                .role(com.pharmacy.common.enums.Role.CUSTOMER)
                .status(com.pharmacy.common.enums.UserStatus.ACTIVE)
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        String jsonBody = Objects.requireNonNull(objectMapper.writeValueAsString(body));
        mockMvc.perform(post("/internal/notifications/dispatch")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value(50))
                .andExpect(jsonPath("$.email").value("user@test.com"));

        verify(notificationService).createNotification(
                1L, NotificationType.ORDER_PLACED, "Hello", "Body", "ref-1");
    }

    @Test
    void markNotificationEmailSent_returns204() throws Exception {
        mockMvc.perform(patch("/internal/notifications/9/email-sent"))
                .andExpect(status().isNoContent());

        verify(notificationService).markEmailSent(9L);
    }
}
