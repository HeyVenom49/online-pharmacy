package com.pharmacy.common.dto.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal contract: notification service asks identity to persist an in-app row and return the user's email for delivery.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationDispatchRequest {

    @NotNull
    private Long userId;

    /**
     * Must match {@code com.pharmacy.identity.entity.Notification.NotificationType} name, e.g. ORDER_PLACED.
     */
    @NotBlank
    private String type;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    private String referenceId;
}
