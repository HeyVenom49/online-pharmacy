package com.pharmacy.identity.controller;

import com.pharmacy.common.dto.ApiResponse;
import com.pharmacy.common.dto.PageResponse;
import com.pharmacy.identity.dto.NotificationDTO;
import com.pharmacy.identity.security.JwtUserPrincipal;
import com.pharmacy.identity.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notification management APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get notifications", description = "Returns paginated notifications for the authenticated user")
    public ResponseEntity<ApiResponse<PageResponse<NotificationDTO>>> getNotifications(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<NotificationDTO> notifications = notificationService
                .getUserNotifications(principal.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications", description = "Returns all unread notifications for the authenticated user")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getUnreadNotifications(
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        List<NotificationDTO> notifications = notificationService
                .getUnreadNotifications(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread count", description = "Returns the count of unread notifications")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        long count = notificationService.getUnreadCount(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark as read", description = "Marks a specific notification as read")
    public ResponseEntity<ApiResponse<NotificationDTO>> markAsRead(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long id) {
        NotificationDTO notification = notificationService.markAsRead(id, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all as read", description = "Marks all unread notifications as read")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllAsRead(
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        int updated = notificationService.markAllAsRead(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read",
                Map.of("updated", updated)));
    }
}
