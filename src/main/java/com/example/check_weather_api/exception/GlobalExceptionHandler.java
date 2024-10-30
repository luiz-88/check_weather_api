package com.example.check_weather_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidApiKeyException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleInvalidApiKeyException() {
        return "Invalid API Key.";
    }

    @ExceptionHandler(RateLimitExceededException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public String handleRateLimitExceededException() {
        return "Hourly rate limit exceeded.";
    }
}

