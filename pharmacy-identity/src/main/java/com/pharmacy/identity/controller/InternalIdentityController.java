package com.pharmacy.identity.controller;

import com.pharmacy.common.dto.notification.CreateNotificationDispatchRequest;
import com.pharmacy.common.dto.notification.NotificationDispatchResponse;
import com.pharmacy.common.exception.ResourceNotFoundException;
import com.pharmacy.identity.dto.TokenValidationResponse;
import com.pharmacy.identity.dto.NotificationDTO;
import com.pharmacy.identity.entity.Notification.NotificationType;
import com.pharmacy.identity.repository.UserRepository;
import com.pharmacy.identity.service.AuthService;
import com.pharmacy.identity.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalIdentityController {

    private final AuthService authService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping("/token/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestParam String token) {
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(TokenValidationResponse.builder()
                .valid(isValid)
                .build());
    }

    /**
     * Called by pharmacy-notifications: create in-app notification row and return email for channel delivery.
     */
    @PostMapping("/notifications/dispatch")
    public ResponseEntity<NotificationDispatchResponse> dispatchNotification(
            @Valid @RequestBody CreateNotificationDispatchRequest request) {
        NotificationType type = NotificationType.valueOf(request.getType());
        NotificationDTO dto = notificationService.createNotification(
                request.getUserId(),
                type,
                request.getTitle(),
                request.getMessage(),
                request.getReferenceId());
        String email = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()))
                .getEmail();
        return ResponseEntity.ok(NotificationDispatchResponse.builder()
                .notificationId(dto.getId())
                .email(email)
                .build());
    }

    @PatchMapping("/notifications/{id}/email-sent")
    public ResponseEntity<Void> markNotificationEmailSent(@PathVariable Long id) {
        notificationService.markEmailSent(id);
        return ResponseEntity.noContent().build();
    }
}
