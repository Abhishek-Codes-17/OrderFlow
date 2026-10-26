package com.abhishek.orderflow.controller;

import com.abhishek.orderflow.pricing.PricingClient;
import com.abhishek.orderflow.pricing.PricingRequest;
import com.abhishek.orderflow.pricing.PricingResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    private final PricingClient pricingClient;

    public PricingController(
            PricingClient pricingClient) {
        this.pricingClient = pricingClient;
    }

    @PostMapping
    public ResponseEntity<PricingResponse> calculatePrice(
            @RequestBody PricingRequest request) {



        PricingResponse response =
                pricingClient.calculatePrice(request);

        return ResponseEntity.ok(response);
    }
}
