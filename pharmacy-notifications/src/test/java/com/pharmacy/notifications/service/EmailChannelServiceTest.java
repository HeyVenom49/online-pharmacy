package com.pharmacy.notifications.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailChannelServiceTest {

    @Test
    void deliver_returnsFalseWhenNoMailSender() {
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        EmailChannelService svc = new EmailChannelService(provider);
        ReflectionTestUtils.setField(svc, "fromEmail", "from@test.com");
        assertFalse(svc.deliver("to@test.com", "s", "b"));
    }

    @Test
    void deliver_sendsAndReturnsTrue() {
        JavaMailSender sender = mock(JavaMailSender.class);
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(sender);
        EmailChannelService svc = new EmailChannelService(provider);
        ReflectionTestUtils.setField(svc, "fromEmail", "from@test.com");
        assertTrue(svc.deliver("to@test.com", "sub", "body"));
        verify(sender).send(any(SimpleMailMessage.class));
    }

    @Test
    void deliver_returnsFalseOnSendFailure() {
        JavaMailSender sender = mock(JavaMailSender.class);
        doThrow(new RuntimeException("smtp")).when(sender).send(any(SimpleMailMessage.class));
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(sender);
        EmailChannelService svc = new EmailChannelService(provider);
        ReflectionTestUtils.setField(svc, "fromEmail", "from@test.com");
        assertFalse(svc.deliver("to@test.com", "s", "b"));
    }
}
