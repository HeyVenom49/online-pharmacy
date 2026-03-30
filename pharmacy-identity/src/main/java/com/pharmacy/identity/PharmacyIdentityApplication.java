package com.pharmacy.identity;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"com.pharmacy.identity", "com.pharmacy.common"})
@EnableRabbit
@ComponentScan({"com.pharmacy.identity", "com.pharmacy.common"})
public class PharmacyIdentityApplication {
    public static void main(String[] args) {
        SpringApplication.run(PharmacyIdentityApplication.class, args);
    }
}
