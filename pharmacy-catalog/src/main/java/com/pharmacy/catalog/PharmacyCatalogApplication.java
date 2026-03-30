package com.pharmacy.catalog;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.pharmacy.catalog", "com.pharmacy.common"}, exclude = {
    RedisAutoConfiguration.class,
    RedisRepositoriesAutoConfiguration.class
})
@EnableScheduling
@EnableRabbit
@ComponentScan({"com.pharmacy.catalog", "com.pharmacy.common"})
public class PharmacyCatalogApplication {
    public static void main(String[] args) {
        SpringApplication.run(PharmacyCatalogApplication.class, args);
    }
}
