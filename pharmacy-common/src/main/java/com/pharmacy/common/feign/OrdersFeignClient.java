package com.pharmacy.common.feign;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;

@FeignClient(name = "pharmacy-orders", fallbackFactory = OrdersFeignClientFallbackFactory.class)
public interface OrdersFeignClient {

    @CircuitBreaker(name = "ordersService", fallbackMethod = "getAllOrdersFallback")
    @Retry(name = "ordersService")
    @GetMapping("/internal/orders")
    List<OrderSummaryDTO> getAllOrders();

    default List<OrderSummaryDTO> getAllOrdersFallback(Throwable t) {
        return Collections.emptyList();
    }
}
