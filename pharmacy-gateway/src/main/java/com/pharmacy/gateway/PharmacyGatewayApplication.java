package com.pharmacy.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"com.pharmacy.gateway", "com.pharmacy.common"})
@ComponentScan({"com.pharmacy.gateway", "com.pharmacy.common"})
public class PharmacyGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(PharmacyGatewayApplication.class, args);
    }
}
