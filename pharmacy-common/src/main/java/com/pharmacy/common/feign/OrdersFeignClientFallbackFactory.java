package com.pharmacy.common.feign;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class OrdersFeignClientFallbackFactory implements FallbackFactory<OrdersFeignClient> {
    
    private static final Logger log = LoggerFactory.getLogger(OrdersFeignClientFallbackFactory.class);

    @Override
    public OrdersFeignClient create(Throwable cause) {
        log.error("OrdersFeignClient fallback triggered: {}", cause.getMessage(), cause);
        
        return new OrdersFeignClient() {
            @Override
            public List<OrderSummaryDTO> getAllOrders() {
                log.warn("Fallback: getAllOrders");
                return Collections.emptyList();
            }
        };
    }
}
