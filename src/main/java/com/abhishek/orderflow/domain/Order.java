package com.abhishek.orderflow.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String instrumentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide side;

    @Column(nullable = false)
    private long quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    @Column(precision = 19, scale = 4)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    protected Order() {
    }

    public Order(
            UUID id,
            String instrumentId,
            OrderSide side,
            long quantity,
            OrderType orderType,
            BigDecimal price,
            OrderStatus status,
            Instant createdAt) {

        this.id = id;
        this.instrumentId = instrumentId;
        this.side = side;
        this.quantity = quantity;
        this.orderType = orderType;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
    }

    public void validate() {

        if (status != OrderStatus.RECEIVED) {
            throw new IllegalStateException(
                    "Only RECEIVED orders can be validated"
            );
        }

        status = OrderStatus.VALIDATED;
    }

    public void accept() {

        if (status != OrderStatus.VALIDATED) {
            throw new IllegalStateException(
                    "Only VALIDATED orders can be accepted"
            );
        }

        status = OrderStatus.ACCEPTED;
    }

    public void reject() {

        if (status != OrderStatus.RECEIVED &&
                status != OrderStatus.VALIDATED) {

            throw new IllegalStateException(
                    "Order cannot be rejected from status: " + status
            );
        }

        status = OrderStatus.REJECTED;
    }

    public void cancel() {

        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Order is already cancelled"
            );
        }

        if (status == OrderStatus.REJECTED) {
            throw new IllegalStateException(
                    "Rejected order cannot be cancelled"
            );
        }

        status = OrderStatus.CANCELLED;
    }

    public UUID getId() {
        return id;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public OrderSide getSide() {
        return side;
    }

    public long getQuantity() {
        return quantity;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
