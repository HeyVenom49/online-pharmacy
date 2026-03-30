package com.pharmacy.notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email channel; add {@code SmsChannelService}, {@code PushChannelService} later and route from {@link NotificationDispatchCoordinator}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailChannelService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.username:noreply@pharmacy.com}")
    private String fromEmail;

    public boolean deliver(String to, String subject, String body) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.debug("Skipping email (no JavaMailSender — configure spring.mail.host / MAIL_*)");
            return false;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to={} subject={}", to, subject);
            return true;
        } catch (Exception e) {
            log.error("Email failed to={} subject={}: {}", to, subject, e.getMessage(), e);
            return false;
        }
    }
}
