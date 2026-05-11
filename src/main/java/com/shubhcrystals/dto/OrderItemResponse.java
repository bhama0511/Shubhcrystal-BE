package com.shubhcrystals.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        String productImageUrl,
        String productEmoji,
        BigDecimal price,
        Integer quantity
) {}
