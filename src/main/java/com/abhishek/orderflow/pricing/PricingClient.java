package com.abhishek.orderflow.pricing;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.net.http.HttpClient;

@Component
public class PricingClient {

    private final RestClient restClient;
    private final PricingCache pricingCache;

    public PricingClient(
            PricingCache pricingCache,
            @Value("${orderflow.pricing.base-url}")
            String pricingServiceUrl) {

        this.pricingCache = pricingCache;

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        this.restClient = RestClient.builder()
                .baseUrl(pricingServiceUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public PricingResponse calculatePrice(
            PricingRequest request) {

        BigDecimal cachedPrice = pricingCache.get(
                request.instrumentId(),
                request.side(),
                request.quantity(),
                request.referencePrice()
        );

        if (cachedPrice != null) {

            System.out.println(
                    "REDIS CACHE HIT -> "
                            + request.instrumentId()
                            + " -> "
                            + cachedPrice
            );

            return new PricingResponse(
                    request.instrumentId(),
                    request.side(),
                    request.quantity(),
                    request.referencePrice(),
                    cachedPrice
            );
        }

        System.out.println(
                "REDIS CACHE MISS -> "
                        + request.instrumentId()
                        + " -> calling FastAPI"
        );

        PricingResponse response = restClient
                .post()
                .uri("/api/pricing")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(PricingResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "Pricing service returned an empty response"
            );
        }

        pricingCache.put(
                request.instrumentId(),
                request.side(),
                request.quantity(),
                request.referencePrice(),
                response.calculatedPrice()
        );

        return response;
    }
}