package com.abhishek.orderflow.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Order createOrder() {
        return new Order(
                UUID.randomUUID(),
                "AAPL",
                OrderSide.BUY,
                100,
                OrderType.LIMIT,
                new BigDecimal("200.00"),
                OrderStatus.RECEIVED,
                Instant.now()
        );
    }

    @Test
    void shouldValidateReceivedOrder() {

        Order order = createOrder();

        order.validate();

        assertEquals(
                OrderStatus.VALIDATED,
                order.getStatus()
        );
    }

    @Test
    void shouldNotValidateOrderTwice() {

        Order order = createOrder();

        order.validate();

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        order::validate
                );

        assertEquals(
                "Only RECEIVED orders can be validated",
                exception.getMessage()
        );
    }

    @Test
    void shouldApplyCalculatedPriceToValidatedOrder() {

        Order order = createOrder();

        order.validate();

        order.applyCalculatedPrice(
                new BigDecimal("200.2000")
        );

        assertEquals(
                new BigDecimal("200.2000"),
                order.getCalculatedPrice()
        );
    }

    @Test
    void shouldNotPriceReceivedOrder() {

        Order order = createOrder();

        assertThrows(
                IllegalStateException.class,
                () -> order.applyCalculatedPrice(
                        new BigDecimal("200.2000")
                )
        );
    }

    @Test
    void shouldAcceptPricedValidatedOrder() {

        Order order = createOrder();

        order.validate();

        order.applyCalculatedPrice(
                new BigDecimal("200.2000")
        );

        order.accept();

        assertEquals(
                OrderStatus.ACCEPTED,
                order.getStatus()
        );
    }

    @Test
    void shouldNotAcceptOrderWithoutPricing() {

        Order order = createOrder();

        order.validate();

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        order::accept
                );

        assertEquals(
                "Order must be priced before it can be accepted",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectReceivedOrder() {

        Order order = createOrder();

        order.reject();

        assertEquals(
                OrderStatus.REJECTED,
                order.getStatus()
        );
    }

    @Test
    void shouldCancelReceivedOrder() {

        Order order = createOrder();

        order.cancel();

        assertEquals(
                OrderStatus.CANCELLED,
                order.getStatus()
        );
    }

    @Test
    void shouldNotCancelRejectedOrder() {

        Order order = createOrder();

        order.reject();

        assertThrows(
                IllegalStateException.class,
                order::cancel
        );
    }
}