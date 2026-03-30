package com.pharmacy.notifications.service;

import com.pharmacy.common.dto.notification.CreateNotificationDispatchRequest;
import com.pharmacy.common.dto.notification.NotificationDispatchResponse;
import com.pharmacy.common.feign.IdentityNotificationFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatchCoordinator {

    private final IdentityNotificationFeignClient identityClient;
    private final EmailChannelService emailChannelService;

    /**
     * Persists in-app notification via identity, then sends email. {@code emailOverride} is used for welcome emails
     * where the address may not yet be loaded the same way as stored user email.
     */
    public void dispatchInAppAndEmail(CreateNotificationDispatchRequest request, String emailOverride) {
        NotificationDispatchResponse r = identityClient.dispatch(request);
        String to = emailOverride != null ? emailOverride : r.getEmail();
        if (emailChannelService.deliver(to, request.getTitle(), request.getMessage())) {
            identityClient.markEmailSent(r.getNotificationId());
        }
    }
}
