package com.example.check_weather_api.controller;

import com.example.check_weather_api.exception.InvalidApiKeyException;
import com.example.check_weather_api.exception.RateLimitExceededException;
import com.example.check_weather_api.service.CheckWeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("/api/weather")
public class CheckWeatherController {

    private static final Logger logger = LoggerFactory.getLogger(CheckWeatherController.class);
    private final CheckWeatherService checkWeatherService;

    @Autowired
    public CheckWeatherController(CheckWeatherService checkWeatherService) {
        this.checkWeatherService = checkWeatherService;
    }

    @GetMapping
    public Mono<ResponseEntity<String>> getWeatherDescription(
            @RequestParam String city,
            @RequestParam String country,
            @RequestParam String apiKey) {

        return checkWeatherService.getWeatherDescription(city, country, apiKey)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    // Create a detailed error message
                    String errorMsg = String.format("Error fetching weather data for city: %s, country: %s. Reason: %s",
                            city, country, e.getMessage());

                    // Log the error
                    logger.error(errorMsg, e);

                    // Handle specific exceptions and return appropriate responses
                    return Mono.just(
                            Optional.of(e)
                                    .filter(ex -> ex instanceof InvalidApiKeyException || ex instanceof RateLimitExceededException)
                                    .map(ex -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                            .body("Client error: " + ex.getMessage()))
                                    .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                            .body("Server error: An unexpected issue occurred while fetching weather data."))
                    );
                });
    }
}
