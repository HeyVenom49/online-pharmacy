package com.pharmacy.identity.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String type;
    private String title;
    private String message;
    private String referenceId;
    private Boolean isRead;
    private Boolean emailSent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
