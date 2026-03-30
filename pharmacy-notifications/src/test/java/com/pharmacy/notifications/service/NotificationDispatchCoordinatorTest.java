package com.pharmacy.notifications.service;

import com.pharmacy.common.dto.notification.CreateNotificationDispatchRequest;
import com.pharmacy.common.dto.notification.NotificationDispatchResponse;
import com.pharmacy.common.feign.IdentityNotificationFeignClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchCoordinatorTest {

    @Mock
    private IdentityNotificationFeignClient identityClient;

    @Mock
    private EmailChannelService emailChannelService;

    @InjectMocks
    private NotificationDispatchCoordinator coordinator;

    @Test
    void dispatchInAppAndEmail_sendsEmailAndMarksSent() {
        CreateNotificationDispatchRequest req = CreateNotificationDispatchRequest.builder()
                .userId(1L)
                .type("T")
                .title("t")
                .message("m")
                .referenceId("r")
                .build();
        when(identityClient.dispatch(any())).thenReturn(
                NotificationDispatchResponse.builder().notificationId(5L).email("u@test.com").build());
        when(emailChannelService.deliver("u@test.com", "t", "m")).thenReturn(true);

        coordinator.dispatchInAppAndEmail(req, null);

        verify(identityClient).markEmailSent(5L);
    }

    @Test
    void dispatchInAppAndEmail_usesEmailOverride() {
        CreateNotificationDispatchRequest req = CreateNotificationDispatchRequest.builder()
                .userId(1L)
                .type("T")
                .title("t")
                .message("m")
                .referenceId("r")
                .build();
        when(identityClient.dispatch(any())).thenReturn(
                NotificationDispatchResponse.builder().notificationId(6L).email("u@test.com").build());
        when(emailChannelService.deliver("override@test.com", "t", "m")).thenReturn(true);

        coordinator.dispatchInAppAndEmail(req, "override@test.com");

        verify(identityClient).markEmailSent(6L);
    }

    @Test
    void dispatchInAppAndEmail_skipsMarkWhenEmailFails() {
        CreateNotificationDispatchRequest req = CreateNotificationDispatchRequest.builder()
                .userId(1L)
                .type("T")
                .title("t")
                .message("m")
                .referenceId("r")
                .build();
        when(identityClient.dispatch(any())).thenReturn(
                NotificationDispatchResponse.builder().notificationId(7L).email("u@test.com").build());
        when(emailChannelService.deliver(any(), any(), any())).thenReturn(false);

        coordinator.dispatchInAppAndEmail(req, null);

        verify(identityClient, never()).markEmailSent(any());
    }
}
