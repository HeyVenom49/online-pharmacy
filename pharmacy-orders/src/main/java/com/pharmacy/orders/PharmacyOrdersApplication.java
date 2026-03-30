package com.pharmacy.orders;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.pharmacy.orders", "com.pharmacy.common"}, exclude = {
    RedisAutoConfiguration.class,
    RedisRepositoriesAutoConfiguration.class
})
@EnableFeignClients(basePackages = "com.pharmacy.common.feign")
@EnableRabbit
@EnableScheduling
@ComponentScan({"com.pharmacy.orders", "com.pharmacy.common"})
public class PharmacyOrdersApplication {
    public static void main(String[] args) {
        SpringApplication.run(PharmacyOrdersApplication.class, args);
    }
}
