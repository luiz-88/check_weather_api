package com.example.check_weather_api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
@Component
public class ApiErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiErrorHandler.class);

    public Mono<String> handleApiError(Throwable error, String city, String country) {
        if (error instanceof WebClientResponseException webEx) {
            HttpStatus status = HttpStatus.resolve(webEx.getStatusCode().value()); // Convert HttpStatusCode to HttpStatus

            if (status != null) {
                logger.error("Error occurred during API call to OpenWeatherMap for city: {}, country: {} - Status: {}, Message: {}",
                        city, country, status, webEx.getMessage());

                // Return response based on status code
                if (status.is4xxClientError()) {
                    return Mono.just("Client error: Please verify request parameters.");
                } else if (status.is5xxServerError()) {
                    return Mono.just("Server error: Please try again later.");
                }
            }
        }
        // Default to 500 if unknown error
        logger.error("Unknown error occurred for city: {}, country: {}", city, country, error);
        return Mono.just("Internal error occurred.");
    }
}

