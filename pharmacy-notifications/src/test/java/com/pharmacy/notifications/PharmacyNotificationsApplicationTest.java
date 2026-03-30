package com.pharmacy.notifications;

import com.pharmacy.common.feign.IdentityNotificationFeignClient;
import com.pharmacy.notifications.listener.NotificationDomainEventListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "management.tracing.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.actuate.autoconfigure.tracing.BraveAutoConfiguration"
})
@MockBean(IdentityNotificationFeignClient.class)
class PharmacyNotificationsApplicationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertNotNull(context.getBean(NotificationDomainEventListener.class));
        assertNotNull(context.getBean(PharmacyNotificationsApplication.class));
    }
}
