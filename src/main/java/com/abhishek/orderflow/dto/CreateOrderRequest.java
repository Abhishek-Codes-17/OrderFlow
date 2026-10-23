package com.abhishek.orderflow.dto;

import com.abhishek.orderflow.domain.OrderSide;
import com.abhishek.orderflow.domain.OrderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateOrderRequest(

        @NotBlank(message = "Instrument ID is required")
        String instrumentId,

        @NotNull(message = "Order side is required")
        OrderSide side,

        @Positive(message = "Quantity must be greater than zero")
        long quantity,

        @NotNull(message = "Order type is required")
        OrderType orderType,

        @Positive(message = "Price must be greater than zero")
        BigDecimal price

) {
}
