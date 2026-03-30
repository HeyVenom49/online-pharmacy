package com.pharmacy.orders.controller;

import com.pharmacy.orders.dto.*;
import com.pharmacy.orders.security.JwtUserPrincipal;
import com.pharmacy.orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order and Cart Management APIs")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/cart")
    @Operation(summary = "Get cart", description = "Returns the current user's cart")
    public ResponseEntity<CartDTO> getCart(@AuthenticationPrincipal JwtUserPrincipal principal) {
        return ResponseEntity.ok(orderService.getCart(principal.getUserId()));
    }

    @PostMapping("/cart/items")
    @Operation(summary = "Add to cart", description = "Adds an item to the cart")
    public ResponseEntity<CartDTO> addToCart(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(orderService.addToCart(principal.getUserId(), request));
    }

    @PutMapping("/cart/items/{itemId}")
    @Operation(summary = "Update cart item", description = "Updates item quantity in cart")
    public ResponseEntity<CartDTO> updateCartItem(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(orderService.updateCartItem(principal.getUserId(), itemId, quantity));
    }

    @DeleteMapping("/cart/items/{itemId}")
    @Operation(summary = "Remove from cart", description = "Removes an item from the cart")
    public ResponseEntity<Void> removeFromCart(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long itemId) {
        orderService.removeFromCart(principal.getUserId(), itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cart")
    @Operation(summary = "Clear cart", description = "Removes all items from the cart")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal JwtUserPrincipal principal) {
        orderService.clearCart(principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout/start")
    @Operation(summary = "Start checkout", description = "Initiates the checkout process")
    public ResponseEntity<OrderDTO> startCheckout(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(orderService.startCheckout(principal.getUserId(), request));
    }

    @PostMapping("/checkout/payment")
    @Operation(summary = "Initiate payment", description = "Starts the payment process")
    public ResponseEntity<OrderDTO> initiatePayment(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam Long orderId,
            @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(orderService.initiatePayment(principal.getUserId(), orderId, request));
    }

    @PostMapping("/checkout/confirm")
    @Operation(summary = "Confirm payment", description = "Confirms payment and places the order")
    public ResponseEntity<OrderDTO> confirmPayment(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam Long orderId,
            @RequestParam(required = false) String transactionId) {
        String txId = transactionId != null ? transactionId : "TXN-" + System.currentTimeMillis();
        return ResponseEntity.ok(orderService.confirmPayment(principal.getUserId(), orderId, txId));
    }

    @GetMapping
    @Operation(summary = "Get user orders", description = "Returns all orders for the current user")
    public ResponseEntity<List<OrderDTO>> getUserOrders(@AuthenticationPrincipal JwtUserPrincipal principal) {
        return ResponseEntity.ok(orderService.getUserOrders(principal.getUserId()));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details", description = "Returns details of a specific order")
    public ResponseEntity<OrderDTO> getOrder(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(principal.getUserId(), orderId));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancels an order")
    public ResponseEntity<OrderDTO> cancelOrder(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(principal.getUserId(), orderId));
    }
}
