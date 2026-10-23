package com.abhishek.orderflow.outbox;

import com.abhishek.orderflow.event.OrderCreatedEvent;
import com.abhishek.orderflow.messaging.OrderEventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final OrderEventPublisher orderEventPublisher;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            OrderEventPublisher orderEventPublisher,
            ObjectMapper objectMapper) {

        this.outboxEventRepository = outboxEventRepository;
        this.orderEventPublisher = orderEventPublisher;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxEventRepository
                        .findTop100ByStatusOrderByCreatedAtAsc(
                                OutboxStatus.PENDING
                        );

        for (OutboxEvent outboxEvent : events) {

            try {

                OrderCreatedEvent event =
                        objectMapper.readValue(
                                outboxEvent.getPayload(),
                                OrderCreatedEvent.class
                        );

                orderEventPublisher.publishOrderCreated(event);

                outboxEvent.markPublished();

            } catch (JsonProcessingException exception) {

                System.err.println(
                        "Failed to deserialize outbox event: "
                                + outboxEvent.getId()
                );
            }
        }
    }
}