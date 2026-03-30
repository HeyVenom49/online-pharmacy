package com.pharmacy.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"com.pharmacy.admin", "com.pharmacy.common"}, exclude = {
    RedisAutoConfiguration.class,
    RedisRepositoriesAutoConfiguration.class,
    RabbitAutoConfiguration.class
})
@EnableFeignClients(basePackages = "com.pharmacy.common.feign")
@ComponentScan({"com.pharmacy.admin", "com.pharmacy.common"})
public class PharmacyAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(PharmacyAdminApplication.class, args);
    }
}
