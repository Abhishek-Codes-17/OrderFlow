package com.abhishek.orderflow.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class OrderDemo {
    public static void main(String[] args) {

        Order order = new Order(
                UUID.randomUUID(),
                "AAPL",
                OrderSide.BUY,
                100,
                OrderType.LIMIT,
                new BigDecimal("185.50"),
                OrderStatus.RECEIVED,
                Instant.now()
        );

        System.out.println("Order ID: " + order.getId());
        System.out.println("Instrument: " + order.getInstrumentId());
        System.out.println("Side: " + order.getSide());
        System.out.println("Quantity: " + order.getQuantity());
        System.out.println("Order Type: " + order.getOrderType());
        System.out.println("Price: " + order.getPrice());
        System.out.println("Status: " + order.getStatus());
        System.out.println("Created At: " + order.getCreatedAt());
    }
}
