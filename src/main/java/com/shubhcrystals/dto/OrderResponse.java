package com.shubhcrystals.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        String customerName,
        String customerEmail,
        List<OrderItemResponse> items,
        BigDecimal subtotal,
        BigDecimal shipping,
        BigDecimal total,
        String status,
        String shippingName,
        String shippingPhone,
        String shippingAddress,
        String shippingCity,
        String shippingPincode,
        Instant createdAt,
        Instant updatedAt
) {}
