package com.example.check_weather_api.utils;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimit {
    private AtomicInteger requestCount;
    private LocalDateTime firstRequestTime;

    public RateLimit() {
        this.requestCount = new AtomicInteger(0);
        this.firstRequestTime = LocalDateTime.now();
    }

    public AtomicInteger getRequestCount() {
        return requestCount;
    }

    public LocalDateTime getFirstRequestTime() {
        return firstRequestTime;
    }

    public void reset() {
        this.requestCount.set(0);
        this.firstRequestTime = LocalDateTime.now();
    }
}

