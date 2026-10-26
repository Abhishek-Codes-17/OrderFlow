package com.abhishek.orderflow.pricing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

@Component
public class PricingCache {

    private final StringRedisTemplate redisTemplate;
    private final Duration ttl;

    public PricingCache(
            StringRedisTemplate redisTemplate,
            @Value("${orderflow.pricing.cache.ttl-minutes}")
            long ttlMinutes) {

        this.redisTemplate = redisTemplate;
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    public BigDecimal get(
            String instrumentId,
            String side,
            long quantity,
            BigDecimal referencePrice) {

        String key = buildKey(
                instrumentId,
                side,
                quantity,
                referencePrice
        );

        String cachedValue =
                redisTemplate.opsForValue().get(key);

        if (cachedValue == null) {
            return null;
        }

        return new BigDecimal(cachedValue);
    }

    public void put(
            String instrumentId,
            String side,
            long quantity,
            BigDecimal referencePrice,
            BigDecimal calculatedPrice) {

        String key = buildKey(
                instrumentId,
                side,
                quantity,
                referencePrice
        );

        redisTemplate.opsForValue().set(
                key,
                calculatedPrice.toPlainString(),
                ttl
        );
    }

    private String buildKey(
            String instrumentId,
            String side,
            long quantity,
            BigDecimal referencePrice) {

        return "pricing:"
                + instrumentId.toUpperCase()
                + ":"
                + side.toUpperCase()
                + ":"
                + quantity
                + ":"
                + referencePrice.stripTrailingZeros()
                .toPlainString();
    }
}
