package com.abhishek.orderflow.messaging;

import com.abhishek.orderflow.event.OrderCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String orderEventsTopic;

    public OrderEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${orderflow.kafka.topics.order-events}")
            String orderEventsTopic) {

        this.kafkaTemplate = kafkaTemplate;
        this.orderEventsTopic = orderEventsTopic;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {

        try {

            kafkaTemplate.send(
                    orderEventsTopic,
                    event.orderId().toString(),
                    event
            ).get(10, TimeUnit.SECONDS);

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Failed to publish OrderCreatedEvent to Kafka",
                    exception
            );
        }
    }
}
