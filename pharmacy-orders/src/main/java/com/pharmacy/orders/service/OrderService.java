package com.pharmacy.orders.service;

import com.pharmacy.orders.dto.*;
import com.pharmacy.orders.entity.*;
import com.pharmacy.orders.event.OrderEventPublisher;
import com.pharmacy.orders.repository.*;
import com.pharmacy.common.events.InventoryReleasedEvent;
import com.pharmacy.common.events.OrderCancelledEvent;
import com.pharmacy.common.events.OrderPlacedEvent;
import com.pharmacy.common.feign.MedicineInfoDTO;
import com.pharmacy.common.feign.OrderSummaryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CatalogClient catalogClient;
    private final OrderEventPublisher orderEventPublisher;

    private static final double DELIVERY_FEE = 50.0;

    public CartDTO getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return toCartDTO(cart);
    }

    @Transactional
    public CartDTO addToCart(Long userId, AddToCartRequest request) {
        MedicineInfoDTO medicineInfo = catalogClient.getMedicineInfo(request.getMedicineId());
        
        if (medicineInfo == null) {
            throw new com.pharmacy.common.exception.ResourceNotFoundException("Medicine", request.getMedicineId());
        }

        if (Boolean.TRUE.equals(medicineInfo.getRequiresPrescription())) {
            if (request.getPrescriptionId() == null) {
                throw new com.pharmacy.common.exception.BadRequestException(
                        "This medicine requires a prescription. Please upload your prescription first.");
            }
            
            boolean hasValidRx = catalogClient.hasValidPrescription(userId, request.getMedicineId());
            if (!hasValidRx) {
                throw new com.pharmacy.common.exception.BadRequestException(
                        "Your prescription for this medicine is not yet approved. Please wait for admin approval.");
            }
        }

        Cart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository.findByCartIdAndMedicineId(cart.getId(), request.getMedicineId())
                .orElse(null);

        if (item != null) {
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            item = CartItem.builder()
                    .cart(cart)
                    .medicineId(request.getMedicineId())
                    .medicineName(medicineInfo.getName())
                    .quantity(request.getQuantity())
                    .unitPrice(medicineInfo.getPrice())
                    .prescriptionId(request.getPrescriptionId())
                    .build();
            cart.addItem(item);
            cartRepository.save(cart);
        }

        return toCartDTO(cart);
    }

    @Transactional
    public CartDTO updateCartItem(Long userId, Long itemId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Cart item", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new com.pharmacy.common.exception.BadRequestException("Cart item does not belong to user");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return toCartDTO(cart);
    }

    @Transactional
    public void removeFromCart(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Cart item", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new com.pharmacy.common.exception.BadRequestException("Cart item does not belong to user");
        }

        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCartId(cart.getId());
    }

    @Transactional
    public OrderDTO startCheckout(Long userId, CheckoutRequest request) {
        Cart cart = getOrCreateCart(userId);

        if (cart.getItems().isEmpty()) {
            throw new com.pharmacy.common.exception.BadRequestException("Cart is empty");
        }

        for (CartItem cartItem : cart.getItems()) {
            MedicineInfoDTO medicine = catalogClient.getMedicineInfo(cartItem.getMedicineId());
            if (medicine == null) {
                throw new com.pharmacy.common.exception.ResourceNotFoundException("Medicine", cartItem.getMedicineId());
            }

            if (Boolean.TRUE.equals(medicine.getRequiresPrescription())) {
                if (cartItem.getPrescriptionId() == null) {
                    throw new com.pharmacy.common.exception.BadRequestException(
                            "Medicine '" + medicine.getName() + "' requires a prescription. Please add a valid prescription.");
                }

                boolean hasValidRx = catalogClient.hasValidPrescription(userId, cartItem.getMedicineId());
                if (!hasValidRx) {
                    throw new com.pharmacy.common.exception.BadRequestException(
                            "Your prescription for '" + medicine.getName() + "' is not approved. Please wait for admin approval.");
                }
            }

            if (Boolean.FALSE.equals(medicine.getInStock()) || medicine.getStock() < cartItem.getQuantity()) {
                throw new com.pharmacy.common.exception.BadRequestException(
                        "Insufficient stock for '" + medicine.getName() + "'. Available: " + 
                        (medicine.getStock() != null ? medicine.getStock() : 0));
            }

            cartItem.setUnitPrice(medicine.getPrice());
            cartItemRepository.save(cartItem);
        }

        Order order = Order.builder()
                .userId(userId)
                .addressSnapshot(request.getAddress())
                .addressPincode(request.getPincode())
                .deliverySlot(request.getDeliverySlot())
                .notes(request.getNotes())
                .deliveryFee(DELIVERY_FEE)
                .build();

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .medicineId(cartItem.getMedicineId())
                    .medicineName(cartItem.getMedicineName())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .subtotal(cartItem.getSubtotal())
                    .prescriptionId(cartItem.getPrescriptionId())
                    .build();
            order.addItem(orderItem);
        }

        double total = order.getItems().stream().mapToDouble(OrderItem::getSubtotal).sum();
        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);
        return toOrderDTO(savedOrder);
    }

    @Transactional
    public OrderDTO initiatePayment(Long userId, Long orderId, PaymentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Order", orderId));

        if (!order.getUserId().equals(userId)) {
            throw new com.pharmacy.common.exception.UnauthorizedException("Not authorized to access this order");
        }

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(request.getPaymentMethod())
                .amount(order.getGrandTotal())
                .build();

        paymentRepository.save(payment);

        return toOrderDTO(order);
    }

    @Transactional
    public OrderDTO confirmPayment(Long userId, Long orderId, String transactionId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Order", orderId));

        if (!order.getUserId().equals(userId)) {
            throw new com.pharmacy.common.exception.UnauthorizedException("Not authorized");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Payment not found"));

        payment.setStatus(com.pharmacy.common.enums.PaymentStatus.SUCCESS);
        payment.setTransactionId(transactionId);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        order.setStatus(com.pharmacy.common.enums.OrderStatus.PAID);
        order.setOrderedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        clearCart(userId);

        publishOrderPlacedEvent(savedOrder);

        return toOrderDTO(savedOrder);
    }

    private void publishOrderPlacedEvent(Order order) {
        try {
            List<OrderPlacedEvent.OrderItemEvent> items = order.getItems().stream()
                    .map(item -> OrderPlacedEvent.OrderItemEvent.builder()
                            .medicineId(item.getMedicineId())
                            .medicineName(item.getMedicineName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .build())
                    .collect(Collectors.toList());

            OrderPlacedEvent event = OrderPlacedEvent.builder()
                    .orderId(order.getId())
                    .userId(order.getUserId())
                    .grandTotal(order.getGrandTotal())
                    .items(items)
                    .build();

            orderEventPublisher.publishOrderPlaced(event);
        } catch (Exception e) {
            log.error("Failed to publish OrderPlacedEvent for order {}: {}", order.getId(), e.getMessage());
        }
    }

    public List<OrderDTO> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByOrderedAtDesc(userId)
                .stream()
                .map(this::toOrderDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Order", orderId));

        if (!order.getUserId().equals(userId)) {
            throw new com.pharmacy.common.exception.UnauthorizedException("Not authorized");
        }

        return toOrderDTO(order);
    }

    public List<OrderSummaryDTO> getAllOrdersForAdmin() {
        return orderRepository.findAll().stream()
                .map(this::toOrderSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDTO cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Order", orderId));

        if (!order.getUserId().equals(userId)) {
            throw new com.pharmacy.common.exception.UnauthorizedException("Not authorized");
        }

        if (!order.canCancel()) {
            throw new com.pharmacy.common.exception.BadRequestException("Order cannot be cancelled in current status");
        }

        order.setStatus(com.pharmacy.common.enums.OrderStatus.CUSTOMER_CANCELLED);
        Order savedOrder = orderRepository.save(order);

        publishOrderCancelledEvent(savedOrder, "Customer cancelled");

        return toOrderDTO(savedOrder);
    }

    private void publishOrderCancelledEvent(Order order, String reason) {
        try {
            List<OrderCancelledEvent.CancelledItem> cancelledItems = order.getItems().stream()
                    .map(item -> OrderCancelledEvent.CancelledItem.builder()
                            .medicineId(item.getMedicineId())
                            .quantity(item.getQuantity())
                            .build())
                    .collect(Collectors.toList());

            OrderCancelledEvent event = OrderCancelledEvent.builder()
                    .orderId(order.getId())
                    .userId(order.getUserId())
                    .reason(reason)
                    .items(cancelledItems)
                    .build();

            orderEventPublisher.publishOrderCancelled(event);
        } catch (Exception e) {
            log.error("Failed to publish OrderCancelledEvent for order {}: {}", order.getId(), e.getMessage());
        }
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().userId(userId).build();
                    return cartRepository.save(newCart);
                });
    }

    private CartDTO toCartDTO(Cart cart) {
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
                .map(this::toCartItemDTO)
                .collect(Collectors.toList());

        return CartDTO.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(itemDTOs)
                .subtotal(cart.getTotal())
                .totalItems(cart.getItems().stream().mapToInt(CartItem::getQuantity).sum())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartItemDTO toCartItemDTO(CartItem item) {
        return CartItemDTO.builder()
                .id(item.getId())
                .medicineId(item.getMedicineId())
                .medicineName(item.getMedicineName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .prescriptionId(item.getPrescriptionId())
                .addedAt(item.getCreatedAt())
                .build();
    }

    private OrderDTO toOrderDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(this::toOrderItemDTO)
                .collect(Collectors.toList());

        PaymentDTO paymentDTO = null;
        if (order.getPayment() != null) {
            paymentDTO = toPaymentDTO(order.getPayment());
        }

        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .deliveryFee(order.getDeliveryFee())
                .discount(order.getDiscount())
                .grandTotal(order.getGrandTotal())
                .addressSnapshot(order.getAddressSnapshot())
                .addressPincode(order.getAddressPincode())
                .deliverySlot(order.getDeliverySlot())
                .notes(order.getNotes())
                .items(itemDTOs)
                .payment(paymentDTO)
                .orderedAt(order.getOrderedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemDTO toOrderItemDTO(OrderItem item) {
        return OrderItemDTO.builder()
                .id(item.getId())
                .medicineId(item.getMedicineId())
                .medicineName(item.getMedicineName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .prescriptionId(item.getPrescriptionId())
                .build();
    }

    private PaymentDTO toPaymentDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .status(payment.getStatus().name())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .amount(payment.getAmount())
                .paidAt(payment.getPaidAt())
                .build();
    }

    private OrderSummaryDTO toOrderSummaryDTO(Order order) {
        return OrderSummaryDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .deliveryFee(order.getDeliveryFee())
                .discount(order.getDiscount())
                .grandTotal(order.getGrandTotal())
                .orderedAt(order.getOrderedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
