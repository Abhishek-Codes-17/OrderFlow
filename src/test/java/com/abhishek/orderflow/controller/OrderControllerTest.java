package com.abhishek.orderflow.controller;

import com.abhishek.orderflow.domain.OrderSide;
import com.abhishek.orderflow.domain.OrderStatus;
import com.abhishek.orderflow.domain.OrderType;
import com.abhishek.orderflow.dto.OrderResponse;
import com.abhishek.orderflow.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    void shouldCreateOrder() throws Exception {

        UUID orderId = UUID.randomUUID();

        OrderResponse response =
                new OrderResponse(
                        orderId,
                        "AAPL",
                        OrderSide.BUY,
                        100,
                        OrderType.LIMIT,
                        new BigDecimal("200.00"),
                        null,
                        OrderStatus.RECEIVED,
                        Instant.parse(
                                "2026-10-26T20:00:00Z"
                        )
                );

        when(orderService.createOrder(any()))
                .thenReturn(response);

        String requestBody = """
                {
                  "instrumentId": "AAPL",
                  "side": "BUY",
                  "quantity": 100,
                  "orderType": "LIMIT",
                  "price": 200.00
                }
                """;

        mockMvc.perform(
                        post("/api/orders")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id")
                                .value(orderId.toString())
                )
                .andExpect(
                        jsonPath("$.instrumentId")
                                .value("AAPL")
                )
                .andExpect(
                        jsonPath("$.side")
                                .value("BUY")
                )
                .andExpect(
                        jsonPath("$.quantity")
                                .value(100)
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("RECEIVED")
                );
    }

    @Test
    void shouldRejectInvalidOrderRequest()
            throws Exception {

        String requestBody = """
                {
                  "instrumentId": "",
                  "side": "BUY",
                  "quantity": 0,
                  "orderType": "LIMIT",
                  "price": -10
                }
                """;

        mockMvc.perform(
                        post("/api/orders")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }
}