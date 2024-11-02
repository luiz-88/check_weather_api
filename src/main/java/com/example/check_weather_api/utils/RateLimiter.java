package com.example.check_weather_api.utils;

import com.example.check_weather_api.exception.RateLimitExceededException;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
@Component
public class RateLimiter {
    @Getter
    private AtomicInteger requestCount;
    @Getter
    private LocalDateTime firstRequestTime;

    private final Map<String, RateLimiter> rateLimitMap = new ConcurrentHashMap<>();

    public RateLimiter() {
        this.requestCount = new AtomicInteger(0);
        this.firstRequestTime = LocalDateTime.now();
    }

    public void reset() {
        this.requestCount.set(0);
        this.firstRequestTime = LocalDateTime.now();
    }

    /**
     * Enforces the rate limit by checking if the current request exceeds the allowed rate.
     * If the rate limit is exceeded, a RateLimitExceededException is thrown.
     */
    public void enforceRateLimit(String clientApiKey) {
        RateLimiter rateLimit = rateLimitMap.get(clientApiKey);
        LocalDateTime now = LocalDateTime.now();
        Duration durationSinceFirstRequest = Duration.between(rateLimit.getFirstRequestTime(), now);

        // Reset the rate limit if more than an hour has passed
        if (durationSinceFirstRequest.toHours() >= 1) {
            rateLimit.reset();
        }

        // Increment and check request count
        if (rateLimit.getRequestCount().incrementAndGet() > 5) {
//            logger.warn("API key has exceeded its hourly request limit.");
            throw new RateLimitExceededException("Hourly rate limit exceeded for this API key.");
        }
    }

}



