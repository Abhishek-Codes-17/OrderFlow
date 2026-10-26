package com.abhishek.orderflow.pricing;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Component
public class PricingClient {

    private final RestClient restClient;

    public PricingClient() {

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8000")
                .requestFactory(requestFactory)
                .build();
    }

    public PricingResponse calculatePrice(
            PricingRequest request) {

        System.out.println(
                "JAVA SENDING TO FASTAPI -> " + request
        );

        return restClient
                .post()
                .uri("/api/pricing")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(PricingResponse.class);
    }
}