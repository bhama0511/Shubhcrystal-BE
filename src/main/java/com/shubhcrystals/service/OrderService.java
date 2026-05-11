package com.shubhcrystals.service;

import com.shubhcrystals.dto.OrderItemRequest;
import com.shubhcrystals.dto.OrderItemResponse;
import com.shubhcrystals.dto.OrderResponse;
import com.shubhcrystals.dto.PlaceOrderRequest;
import com.shubhcrystals.model.Order;
import com.shubhcrystals.model.OrderItem;
import com.shubhcrystals.model.OrderStatus;
import com.shubhcrystals.model.Product;
import com.shubhcrystals.model.User;
import com.shubhcrystals.repository.OrderRepository;
import com.shubhcrystals.repository.ProductRepository;
import com.shubhcrystals.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private static final BigDecimal SHIPPING_FEE = new BigDecimal("99");
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("999");

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository,
                        EmailService emailService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public OrderResponse placeOrder(String userEmail, PlaceOrderRequest req) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userEmail));

        Order order = new Order();
        order.setUserId(user.getId());
        order.setShippingName(req.getShippingName().trim());
        order.setShippingPhone(req.getShippingPhone().trim());
        order.setShippingAddress(req.getShippingAddress().trim());
        order.setShippingCity(req.getShippingCity().trim());
        order.setShippingPincode(req.getShippingPincode().trim());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItemRequest itemReq : req.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Product not found: " + itemReq.getProductId()));
            if (!product.isAvailable()) {
                throw new IllegalArgumentException(
                        "Product is not available: " + product.getName());
            }

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImageUrl(product.getImageUrl());
            item.setProductEmoji(product.getEmoji());
            item.setPrice(product.getPrice());
            item.setQuantity(itemReq.getQuantity());
            order.addItem(item);

            subtotal = subtotal.add(
                    product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        BigDecimal shipping = subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0
                ? BigDecimal.ZERO
                : SHIPPING_FEE;
        BigDecimal total = subtotal.add(shipping);

        order.setSubtotal(subtotal);
        order.setShipping(shipping);
        order.setTotal(total);

        Order saved = orderRepository.save(order);
        OrderResponse response = toResponse(saved, user);

        try {
            emailService.sendOrderPlaced(response, user);
            emailService.sendAdminNewOrder(response, user);
        } catch (Exception e) {
            log.warn("Order email enqueue failed for order {}: {}", saved.getId(), e.getMessage());
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userEmail));
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(o -> toResponse(o, user))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        return orders.stream()
                .map(o -> {
                    User u = userRepository.findById(o.getUserId()).orElse(null);
                    return toResponse(o, u);
                })
                .toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        OrderStatus previous = order.getStatus();
        order.setStatus(status);
        Order saved = orderRepository.save(order);
        User user = userRepository.findById(saved.getUserId()).orElse(null);
        OrderResponse response = toResponse(saved, user);

        if (previous != status && user != null) {
            try {
                emailService.sendOrderStatusUpdate(response, user, previous.name());
            } catch (Exception e) {
                log.warn("Status email enqueue failed for order {}: {}", saved.getId(), e.getMessage());
            }
        }

        return response;
    }

    private OrderResponse toResponse(Order o, User user) {
        List<OrderItemResponse> items = o.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getId(),
                        i.getProductId(),
                        i.getProductName(),
                        i.getProductImageUrl(),
                        i.getProductEmoji(),
                        i.getPrice(),
                        i.getQuantity()))
                .toList();
        return new OrderResponse(
                o.getId(),
                o.getUserId(),
                user != null ? user.getName() : null,
                user != null ? user.getEmail() : null,
                items,
                o.getSubtotal(),
                o.getShipping(),
                o.getTotal(),
                o.getStatus().name(),
                o.getShippingName(),
                o.getShippingPhone(),
                o.getShippingAddress(),
                o.getShippingCity(),
                o.getShippingPincode(),
                o.getCreatedAt(),
                o.getUpdatedAt()
        );
    }
}
