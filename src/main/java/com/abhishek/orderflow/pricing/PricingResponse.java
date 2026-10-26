package com.abhishek.orderflow.pricing;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record PricingResponse(

        @JsonProperty("instrument_id")
        String instrumentId,

        String side,

        long quantity,

        @JsonProperty("reference_price")
        BigDecimal referencePrice,

        @JsonProperty("calculated_price")
        BigDecimal calculatedPrice
) {
}