package com.abhishek.orderflow.service;

import com.abhishek.orderflow.domain.Order;
import com.abhishek.orderflow.domain.OrderStatus;
import com.abhishek.orderflow.dto.CreateOrderRequest;
import com.abhishek.orderflow.dto.OrderResponse;
import com.abhishek.orderflow.event.OrderCreatedEvent;
import com.abhishek.orderflow.exception.InvalidOrderStateException;
import com.abhishek.orderflow.exception.OrderNotFoundException;
import com.abhishek.orderflow.outbox.OutboxEvent;
import com.abhishek.orderflow.outbox.OutboxEventRepository;
import com.abhishek.orderflow.outbox.OutboxStatus;
import com.abhishek.orderflow.pricing.PricingClient;
import com.abhishek.orderflow.pricing.PricingRequest;
import com.abhishek.orderflow.pricing.PricingResponse;
import com.abhishek.orderflow.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final PricingClient pricingClient;

    public OrderService(
            OrderRepository orderRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper,
            PricingClient pricingClient) {

        this.orderRepository = orderRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.pricingClient = pricingClient;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        Order order = new Order(
                UUID.randomUUID(),
                request.instrumentId(),
                request.side(),
                request.quantity(),
                request.orderType(),
                request.price(),
                OrderStatus.RECEIVED,
                Instant.now()
        );

        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                savedOrder.getId(),
                savedOrder.getInstrumentId(),
                savedOrder.getSide(),
                savedOrder.getQuantity(),
                savedOrder.getOrderType(),
                savedOrder.getPrice(),
                Instant.now()
        );

        String payload;

        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize OrderCreatedEvent",
                    exception
            );
        }

        OutboxEvent outboxEvent = new OutboxEvent(
                event.eventId(),
                savedOrder.getId(),
                "OrderCreated",
                payload,
                OutboxStatus.PENDING,
                Instant.now()
        );

        outboxEventRepository.save(outboxEvent);

        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID id) {

        Order order = findOrder(id);

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse validateOrder(UUID id) {

        Order order = findOrder(id);

        try {
            order.validate();
        } catch (IllegalStateException exception) {
            throw new InvalidOrderStateException(
                    exception.getMessage()
            );
        }

        return toResponse(order);
    }

    @Transactional
    public OrderResponse priceOrder(UUID id) {

        Order order = findOrder(id);

        if (order.getStatus() != OrderStatus.VALIDATED) {
            throw new InvalidOrderStateException(
                    "Only VALIDATED orders can be priced"
            );
        }

        PricingRequest pricingRequest =
                new PricingRequest(
                        order.getInstrumentId(),
                        order.getSide().name(),
                        order.getQuantity(),
                        order.getPrice()
                );

        PricingResponse pricingResponse =
                pricingClient.calculatePrice(pricingRequest);

        try {
            order.applyCalculatedPrice(
                    pricingResponse.calculatedPrice()
            );
        } catch (IllegalStateException |
                 IllegalArgumentException exception) {

            throw new InvalidOrderStateException(
                    exception.getMessage()
            );
        }

        return toResponse(order);
    }

    @Transactional
    public OrderResponse acceptOrder(UUID id) {

        Order order = findOrder(id);

        try {
            order.accept();
        } catch (IllegalStateException exception) {
            throw new InvalidOrderStateException(
                    exception.getMessage()
            );
        }

        return toResponse(order);
    }

    @Transactional
    public OrderResponse rejectOrder(UUID id) {

        Order order = findOrder(id);

        try {
            order.reject();
        } catch (IllegalStateException exception) {
            throw new InvalidOrderStateException(
                    exception.getMessage()
            );
        }

        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(UUID id) {

        Order order = findOrder(id);

        try {
            order.cancel();
        } catch (IllegalStateException exception) {
            throw new InvalidOrderStateException(
                    exception.getMessage()
            );
        }

        return toResponse(order);
    }

    private Order findOrder(UUID id) {

        return orderRepository.findById(id)
                .orElseThrow(
                        () -> new OrderNotFoundException(id)
                );
    }

    private OrderResponse toResponse(Order order) {

        return new OrderResponse(
                order.getId(),
                order.getInstrumentId(),
                order.getSide(),
                order.getQuantity(),
                order.getOrderType(),
                order.getPrice(),
                order.getCalculatedPrice(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
