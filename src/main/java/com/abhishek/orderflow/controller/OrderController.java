package com.abhishek.orderflow.controller;

import com.abhishek.orderflow.dto.CreateOrderRequest;
import com.abhishek.orderflow.dto.OrderResponse;
import com.abhishek.orderflow.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable UUID id) {
        return orderService.getOrder(id);
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PostMapping("/{id}/validate")
    public OrderResponse validateOrder(@PathVariable UUID id) {
        return orderService.validateOrder(id);
    }

    @PostMapping("/{id}/accept")
    public OrderResponse acceptOrder(@PathVariable UUID id) {
        return orderService.acceptOrder(id);
    }

    @PostMapping("/{id}/reject")
    public OrderResponse rejectOrder(@PathVariable UUID id) {
        return orderService.rejectOrder(id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable UUID id) {
        return orderService.cancelOrder(id);
    }
}