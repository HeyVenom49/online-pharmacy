package com.pharmacy.notifications;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

import com.pharmacy.common.config.RabbitMQConfig;
import com.pharmacy.common.exception.GlobalExceptionHandler;
import com.pharmacy.common.feign.IdentityNotificationFeignClient;

/**
 * pharmacy-common includes DB/JWT-oriented beans; this service is stateless (Feign + Rabbit + mail).
 * Scan only {@code com.pharmacy.notifications}; import shared config that has no DataSource requirement.
 * Register only {@link IdentityNotificationFeignClient} so catalog/orders Feign fallbacks are not loaded.
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class
})
@Import({RabbitMQConfig.class, GlobalExceptionHandler.class})
@EnableFeignClients(clients = IdentityNotificationFeignClient.class)
@EnableRabbit
public class PharmacyNotificationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PharmacyNotificationsApplication.class, args);
    }
}
