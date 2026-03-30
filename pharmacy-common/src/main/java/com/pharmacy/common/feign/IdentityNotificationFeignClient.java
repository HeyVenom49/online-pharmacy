package com.pharmacy.common.feign;

import com.pharmacy.common.dto.notification.CreateNotificationDispatchRequest;
import com.pharmacy.common.dto.notification.NotificationDispatchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "pharmacy-identity", contextId = "identityNotifications", path = "/internal")
public interface IdentityNotificationFeignClient {

    @PostMapping("/notifications/dispatch")
    NotificationDispatchResponse dispatch(@RequestBody CreateNotificationDispatchRequest request);

    @PatchMapping("/notifications/{id}/email-sent")
    void markEmailSent(@PathVariable("id") Long id);
}
