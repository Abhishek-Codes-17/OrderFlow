package com.abhishek.orderflow.event;

import com.abhishek.orderflow.domain.OrderSide;
import com.abhishek.orderflow.domain.OrderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        UUID orderId,
        String instrumentId,
        OrderSide side,
        long quantity,
        OrderType orderType,
        BigDecimal price,
        Instant occurredAt
) {
}
