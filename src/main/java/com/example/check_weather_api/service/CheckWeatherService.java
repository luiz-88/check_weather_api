package com.example.check_weather_api.service;

import com.example.check_weather_api.configuration.ApiKeyConfig;
import com.example.check_weather_api.exception.InvalidApiKeyException;
import com.example.check_weather_api.exception.RateLimitExceededException;
import com.example.check_weather_api.model.CheckWeatherData;
import com.example.check_weather_api.model.CheckWeatherResponse;
import com.example.check_weather_api.repository.CheckWeatherRepository;
import com.example.check_weather_api.utils.RateLimit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CheckWeatherService {
    private final CheckWeatherRepository weatherRepository;
    private final WebClient webClient;
    private static final Logger logger = LoggerFactory.getLogger(CheckWeatherService.class);

    private final List<String> validApiKeys;
    @Value("${openweathermap.base.url}")
    private String baseUrl;

    private final Map<String, RateLimit> rateLimitMap = new ConcurrentHashMap<>();

    @Autowired
    public CheckWeatherService(WebClient.Builder webClientBuilder, CheckWeatherRepository weatherRepository, ApiKeyConfig apiKeyConfig) {
        this.webClient = webClientBuilder.baseUrl("http://api.openweathermap.org/data/2.5/weather").build();
        this.weatherRepository = weatherRepository;
        this.validApiKeys = apiKeyConfig.getKeys();
    }

    @Cacheable(cacheNames = "weather", key = "#city + ',' + #country")
    public Mono<String> getWeatherDescription(String city, String country, String clientApiKey) {
        validateApiKey(clientApiKey);
        enforceRateLimit(clientApiKey);

        // Check the database asynchronously for cached data
        return weatherRepository.findByCityAndCountry(city, country)
                .flatMap(data -> Mono.just(data.getDescription()))  // Return description if data exists
                .switchIfEmpty(fetchAndCacheWeather(city, country, clientApiKey));  // Fetch from API if not cached
    }

    private Mono<String> fetchAndCacheWeather(String city, String country, String clientApiKey) {
        String uri = String.format("?q=%s,%s&appid=%s", city, country, clientApiKey);

        // Asynchronously fetch weather data from the downstream API
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(CheckWeatherResponse.class)  // Directly map to WeatherApiResponse
                .flatMap(weatherResponse -> {
                    // Extract the description
                    String description = weatherResponse.getWeather().stream()
                            .findFirst()
                            .map(CheckWeatherResponse.Weather::getDescription)
                            .orElse("No description available");

                    // Cache the description asynchronously in H2
                    return saveWeatherData(city, country, description).thenReturn(description);
                })
                .onErrorResume(e -> {
                    String errorMsg = String.format("Failed to fetch weather data for city: %s, country: %s. Reason: %s", city, country, e.getMessage());
                    logger.error(errorMsg, e);
                    return Mono.just("Error fetching weather data due to connection issues or invalid parameters.");
                });
    }

    private Mono<Void> saveWeatherData(String city, String country, String description) {
        CheckWeatherData data = new CheckWeatherData();
        data.setCity(city);
        data.setCountry(country);
        data.setDescription(description);
        return weatherRepository.save(data).then();
    }


    private void validateApiKey(String clientApiKey) {
        // Ensure the API key is present in the rate limit map
        if (!validApiKeys.contains(clientApiKey)) {
            throw new InvalidApiKeyException("Invalid API key.");
        }
        // Initialize if absent
        rateLimitMap.computeIfAbsent(clientApiKey, k -> new RateLimit());
    }

    private void enforceRateLimit(String clientApiKey) {
        RateLimit rateLimit = rateLimitMap.get(clientApiKey);
        LocalDateTime now = LocalDateTime.now();
        Duration durationSinceFirstRequest = Duration.between(rateLimit.getFirstRequestTime(), now);

        // Reset the rate limit if more than an hour has passed
        if (durationSinceFirstRequest.toHours() >= 1) {
            rateLimit.reset();
        }

        // Increment and check request count
        if (rateLimit.getRequestCount().incrementAndGet() > 5) {
            throw new RateLimitExceededException("Hourly rate limit exceeded for this API key.");
        }
    }
}