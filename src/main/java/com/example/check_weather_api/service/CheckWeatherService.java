package com.example.check_weather_api.service;

import com.example.check_weather_api.exception.InvalidApiKeyException;
import com.example.check_weather_api.exception.RateLimitExceededException;
import com.example.check_weather_api.model.CheckWeatherData;
import com.example.check_weather_api.repository.CheckWeatherRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CheckWeatherService {
    private final CheckWeatherRepository weatherRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final Logger logger = LoggerFactory.getLogger(CheckWeatherService.class);

    @Value("${openweathermap.api.key}")
    private String apiKey;

    @Value("${openweathermap.base.url}")
    private String baseUrl;

    private final ConcurrentHashMap<String, AtomicInteger> rateLimitMap = new ConcurrentHashMap<>();

    public CheckWeatherService(CheckWeatherRepository weatherRepository) {
        this.weatherRepository = weatherRepository;
    }

    public String getWeatherDescription(String city, String country, String clientApiKey) {
        validateApiKey(clientApiKey);
        enforceRateLimit(clientApiKey);

        Optional<CheckWeatherData> cachedData = weatherRepository.findByCityAndCountry(city, country);
        if (cachedData.isPresent()) {
            return cachedData.get().getDescription();
        }
        return fetchAndCacheWeather(city, country);
    }

    private String fetchAndCacheWeather(String city, String country) {
        String uri = String.format("%s?q=%s,%s&appid=%s", baseUrl, city, country, apiKey);
        String response = restTemplate.getForObject(uri, String.class);

        String description = parseDescription(response);
        cacheWeatherData(city, country, description);

        return description;
    }

    private String parseDescription(String response) {
        try {
            // Initialize ObjectMapper
            ObjectMapper mapper = new ObjectMapper();

            // Parse the JSON response into a JsonNode tree
            JsonNode rootNode = mapper.readTree(response);

            // Navigate to the "weather" array and get the first element's "description" field
            JsonNode weatherArray = rootNode.path("weather");
            if (weatherArray.isArray() && !weatherArray.isEmpty()) {
                JsonNode descriptionNode = weatherArray.get(0).path("description");
                return descriptionNode.asText();
            } else {
                throw new RuntimeException("Description field not found in weather data");
            }
        } catch (Exception e) {
            // Handle parsing exceptions
            // Log the exception at ERROR level
            logger.error("Failed to parse description from response", e);
            throw new RuntimeException("Failed to parse description from response", e);
        }
    }

    private void cacheWeatherData(String city, String country, String description) {
        CheckWeatherData data = new CheckWeatherData();
        data.setCity(city);
        data.setCountry(country);
        data.setDescription(description);
        weatherRepository.save(data);
    }

    private void validateApiKey(String clientApiKey) {
        if (!clientApiKey.equals(apiKey)) {
            throw new InvalidApiKeyException("Invalid API key.");
        }
    }

    private void enforceRateLimit(String clientApiKey) {
        rateLimitMap.putIfAbsent(clientApiKey, new AtomicInteger(0));
        AtomicInteger requestCount = rateLimitMap.get(clientApiKey);

        if (requestCount.incrementAndGet() > 5) {
            throw new RateLimitExceededException("Hourly rate limit exceeded.");
        }
    }
}