package com.pharmacy.notifications;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class PharmacyNotificationsApplicationMainTest {

    @Test
    void mainDelegatesToSpringApplication() {
        try (MockedStatic<SpringApplication> boot = mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext ctx = mock(ConfigurableApplicationContext.class);
            boot.when(() -> SpringApplication.run(PharmacyNotificationsApplication.class, new String[]{}))
                    .thenReturn(ctx);

            PharmacyNotificationsApplication.main(new String[]{});

            boot.verify(() -> SpringApplication.run(PharmacyNotificationsApplication.class, new String[]{}));
        }
    }
}
