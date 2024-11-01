package com.example.check_weather_api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

public class ApiErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiErrorHandler.class);

    public Mono<String> handleApiError(Throwable error, String city, String country) {
        if (error instanceof WebClientResponseException webEx) {
            HttpStatus status = HttpStatus.resolve(webEx.getStatusCode().value());
            if (status == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            String errorMessage = status.is4xxClientError() ? "Invalid parameters provided" : "Error fetching weather data";

            logger.error("Failed to fetch weather data for city: {}, country: {}. Status code: {}, Reason: {}",
                    city, country, status, webEx.getMessage());

            return Mono.error(new ResponseStatusException(status, errorMessage));
        }

        logger.error("Unexpected error occurred while fetching weather data for city: {}, country: {}. Reason: {}",
                city, country, error.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error occurred"));
    }
}

