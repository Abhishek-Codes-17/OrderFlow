package com.abhishek.orderflow.service;

import com.abhishek.orderflow.domain.Order;
import com.abhishek.orderflow.domain.OrderSide;
import com.abhishek.orderflow.domain.OrderStatus;
import com.abhishek.orderflow.domain.OrderType;
import com.abhishek.orderflow.dto.OrderResponse;
import com.abhishek.orderflow.outbox.OutboxEventRepository;
import com.abhishek.orderflow.pricing.PricingClient;
import com.abhishek.orderflow.pricing.PricingResponse;
import com.abhishek.orderflow.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private PricingClient pricingClient;

    private OrderService orderService;

    @BeforeEach
    void setUp() {

        orderService = new OrderService(
                orderRepository,
                outboxEventRepository,
                new ObjectMapper(),
                pricingClient
        );
    }

    @Test
    void shouldGetExistingOrder() {

        UUID orderId = UUID.randomUUID();

        Order order = createOrder(
                orderId,
                OrderStatus.RECEIVED
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        OrderResponse response =
                orderService.getOrder(orderId);

        assertEquals(orderId, response.id());
        assertEquals("AAPL", response.instrumentId());
        assertEquals(OrderStatus.RECEIVED, response.status());

        verify(orderRepository).findById(orderId);
    }

    @Test
    void shouldValidateOrder() {

        UUID orderId = UUID.randomUUID();

        Order order = createOrder(
                orderId,
                OrderStatus.RECEIVED
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        OrderResponse response =
                orderService.validateOrder(orderId);

        assertEquals(
                OrderStatus.VALIDATED,
                response.status()
        );
    }

    @Test
    void shouldCallPricingServiceAndApplyPrice() {

        UUID orderId = UUID.randomUUID();

        Order order = createOrder(
                orderId,
                OrderStatus.RECEIVED
        );

        order.validate();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        PricingResponse pricingResponse =
                new PricingResponse(
                        "AAPL",
                        "BUY",
                        100,
                        new BigDecimal("200.00"),
                        new BigDecimal("200.2000")
                );

        when(pricingClient.calculatePrice(any()))
                .thenReturn(pricingResponse);

        OrderResponse response =
                orderService.priceOrder(orderId);

        assertEquals(
                new BigDecimal("200.2000"),
                response.calculatedPrice()
        );

        assertEquals(
                OrderStatus.VALIDATED,
                response.status()
        );

        verify(pricingClient, times(1))
                .calculatePrice(any());
    }

    @Test
    void shouldAcceptPricedOrder() {

        UUID orderId = UUID.randomUUID();

        Order order = createOrder(
                orderId,
                OrderStatus.RECEIVED
        );

        order.validate();

        order.applyCalculatedPrice(
                new BigDecimal("200.2000")
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        OrderResponse response =
                orderService.acceptOrder(orderId);

        assertEquals(
                OrderStatus.ACCEPTED,
                response.status()
        );
    }

    private Order createOrder(
            UUID id,
            OrderStatus status) {

        return new Order(
                id,
                "AAPL",
                OrderSide.BUY,
                100,
                OrderType.LIMIT,
                new BigDecimal("200.00"),
                status,
                Instant.now()
        );
    }
}
