package com.abhishek.orderflow.dto;

import com.abhishek.orderflow.domain.OrderSide;
import com.abhishek.orderflow.domain.OrderStatus;
import com.abhishek.orderflow.domain.OrderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String instrumentId,
        OrderSide side,
        long quantity,
        OrderType orderType,
        BigDecimal price,
        OrderStatus status,
        Instant createdAt
) {

}
