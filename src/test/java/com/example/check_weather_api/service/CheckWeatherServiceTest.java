package com.example.check_weather_api.service;

import com.example.check_weather_api.repository.CheckWeatherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ConcurrentHashMap;

@ExtendWith(MockitoExtension.class)
public class CheckWeatherServiceTest {

    @Mock
    private CheckWeatherRepository weatherRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CheckWeatherService checkWeatherService;

    @Value("${openweathermap.api.key}")
    private String apiKey = "test-api-key";

    @Value("${openweathermap.base.url}")
    private String baseUrl = "http://api.openweathermap.org/data/2.5/weather";

    @BeforeEach
    public void setUp() {
        // Set mock values for apiKey and baseUrl using ReflectionTestUtils
        ReflectionTestUtils.setField(checkWeatherService, "apiKey", apiKey);
        ReflectionTestUtils.setField(checkWeatherService, "baseUrl", baseUrl);

        // Initialize a new rate limit map for each test
        ReflectionTestUtils.setField(checkWeatherService, "rateLimitMap", new ConcurrentHashMap<>());
    }

    // Test case for a valid API call with no cached data
}

