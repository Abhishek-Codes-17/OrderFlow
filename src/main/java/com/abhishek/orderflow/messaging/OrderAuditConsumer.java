package com.abhishek.orderflow.messaging;

import com.abhishek.orderflow.event.OrderCreatedEvent;
import com.abhishek.orderflow.idempotency.ProcessedEvent;
import com.abhishek.orderflow.idempotency.ProcessedEventRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class OrderAuditConsumer {

    private final ProcessedEventRepository processedEventRepository;

    public OrderAuditConsumer(
            ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(
            topics = "${orderflow.kafka.topics.order-events}",
            groupId = "orderflow-audit"
    )
    @Transactional
    public void consumeOrderCreated(OrderCreatedEvent event) {

        // 1. Ignore events that have already been processed
        if (processedEventRepository.existsById(event.eventId())) {

            System.out.println(
                    "DUPLICATE EVENT IGNORED -> eventId="
                            + event.eventId()
            );

            return;
        }

        // 2. TEMPORARY failure simulation for retry/DLT testing
        if ("FAIL".equalsIgnoreCase(event.instrumentId())) {

            System.out.println(
                    "SIMULATED PROCESSING FAILURE -> eventId="
                            + event.eventId()
            );

            throw new RuntimeException(
                    "Simulated audit consumer failure"
            );
        }

        // 3. Process the event normally
        System.out.println(
                "AUDIT EVENT PROCESSED -> " +
                        "eventId=" + event.eventId() +
                        ", orderId=" + event.orderId() +
                        ", instrument=" + event.instrumentId() +
                        ", side=" + event.side() +
                        ", quantity=" + event.quantity()
        );

        // 4. Record successful processing
        ProcessedEvent processedEvent =
                new ProcessedEvent(
                        event.eventId(),
                        Instant.now()
                );

        processedEventRepository.save(processedEvent);
    }
}