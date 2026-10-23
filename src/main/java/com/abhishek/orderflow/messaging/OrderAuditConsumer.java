package com.abhishek.orderflow.messaging;

import com.abhishek.orderflow.event.OrderCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderAuditConsumer {

    @KafkaListener(
            topics = "${orderflow.kafka.topics.order-events}",
            groupId = "orderflow-audit"
    )
    public void consumeOrderCreated(OrderCreatedEvent event) {

        System.out.println(
                "AUDIT EVENT RECEIVED -> " +
                        "eventId=" + event.eventId() +
                        ", orderId=" + event.orderId() +
                        ", instrument=" + event.instrumentId() +
                        ", side=" + event.side() +
                        ", quantity=" + event.quantity()
        );
    }
}