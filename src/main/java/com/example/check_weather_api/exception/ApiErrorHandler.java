package com.example.check_weather_api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
@Component
public class ApiErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiErrorHandler.class);

    public Mono<ResponseEntity<String>> handleApiError(Throwable error, String city, String country) {
        if (error instanceof WebClientResponseException webEx) {
            HttpStatus status = HttpStatus.valueOf(webEx.getStatusCode().value());
            String errorMessage = webEx.getResponseBodyAsString(); // Get the error message from the downstream API

            logger.error("Error occurred during API call to OpenWeatherMap for city: {}, country: {} - Status: {}, Message: {}",
                    city, country, status, errorMessage);

            // Return a ResponseEntity with the status and error message
            return Mono.just(ResponseEntity.status(status).body(errorMessage.isEmpty() ? "Error: " + status.getReasonPhrase() : errorMessage));
        }

        // Default to 500 if unknown error
        logger.error("Unknown error occurred for city: {}, country: {}", city, country, error);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error occurred."));
    }
}

