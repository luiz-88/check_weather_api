package com.example.check_weather_api.controller;

import com.example.check_weather_api.exception.ApiErrorHandler;
import com.example.check_weather_api.exception.InvalidApiKeyException;
import com.example.check_weather_api.exception.RateLimitExceededException;
import com.example.check_weather_api.service.CheckWeatherService;
import com.example.check_weather_api.utils.ApiKeyValidator;
import com.example.check_weather_api.utils.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/weather")
public class CheckWeatherController {

    private static final Logger logger = LoggerFactory.getLogger(CheckWeatherController.class);
    private final CheckWeatherService checkWeatherService;
    private final ApiKeyValidator apiKeyValidator;
    private final RateLimiter rateLimiter;

    private final ApiErrorHandler apiErrorHandler;

    @Autowired
    public CheckWeatherController(CheckWeatherService checkWeatherService,
                                  ApiErrorHandler apiErrorHandler,
                                  ApiKeyValidator apiKeyValidator,
                                  RateLimiter rateLimiter) {
        this.checkWeatherService = checkWeatherService;
        this.apiErrorHandler = apiErrorHandler;
        this.apiKeyValidator = apiKeyValidator;
        this.rateLimiter = rateLimiter;

    }

    @GetMapping
    public Mono<ResponseEntity<String>> getWeatherDescription(
            @RequestParam String city,
            @RequestParam String country,
            @RequestParam String apiKey) {

        try {
            // Validate the API key and enforce the rate limit outside the cacheable method
            apiKeyValidator.validate(apiKey);
            rateLimiter.enforceRateLimit(apiKey);

            // Call the cacheable service method if the API key is valid
            return checkWeatherService.getWeatherDescription(city, country, apiKey)
                    .map(ResponseEntity::ok);
        } catch (InvalidApiKeyException e) {
            logger.warn("Invalid API key: {}", apiKey);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Error: Invalid API key provided. Please check your API key and try again."));
        } catch (RateLimitExceededException e) {
            logger.warn("Rate limit exceeded: {}", e.getMessage());
            return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Error: API rate limit exceeded. Please try again later."));
        }
    }
}
