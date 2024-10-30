package com.example.check_weather_api.service;

import com.example.check_weather_api.exception.InvalidApiKeyException;
import com.example.check_weather_api.exception.RateLimitExceededException;
import com.example.check_weather_api.model.CheckWeatherData;
import com.example.check_weather_api.repository.CheckWeatherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckWeatherServiceTest {

    @Mock
    private RestTemplate restTemplate;  // Mocked RestTemplate

    @Mock
    private CheckWeatherRepository weatherRepository;

    @InjectMocks
    private CheckWeatherService checkWeatherService;

    private final String validApiKey = "test_api_key";
    private final String baseUrl = "https://api.openweathermap.org/data/2.5/weather";

    @BeforeEach
    void setUp() {
        // Inject the mocked RestTemplate into CheckWeatherService
        ReflectionTestUtils.setField(checkWeatherService, "restTemplate", restTemplate);

        // Inject API key and base URL into CheckWeatherService
        ReflectionTestUtils.setField(checkWeatherService, "apiKey", validApiKey);
        ReflectionTestUtils.setField(checkWeatherService, "baseUrl", baseUrl);

        // Use lenient() to avoid UnnecessaryStubbingException
        lenient().when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{\"weather\":[{\"description\":\"clear sky\"}]}");

        // Reset the rate limit map to ensure isolation across tests
        ReflectionTestUtils.setField(checkWeatherService, "rateLimitMap", new ConcurrentHashMap<>());
    }

    @Test
    void testGetWeatherDescription_RateLimitExceeded_ThrowsRateLimitExceededException() {
        String city = "Sydney";
        String country = "AU";

        // Simulate 5 requests to reach the rate limit
        for (int i = 0; i < 5; i++) {
            checkWeatherService.getWeatherDescription(city, country, validApiKey);
        }

        // Exceed rate limit and expect RateLimitExceededException
        RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class,
                () -> checkWeatherService.getWeatherDescription(city, country, validApiKey)
        );

        assertEquals("Hourly rate limit exceeded.", exception.getMessage());
    }

    @Test
    void testGetWeatherDescription_InvalidApiKey_ThrowsInvalidApiKeyException() {
        String city = "Melbourne";
        String country = "AU";
        String invalidApiKey = "invalid_key";

        InvalidApiKeyException exception = assertThrows(
                InvalidApiKeyException.class,
                () -> checkWeatherService.getWeatherDescription(city, country, invalidApiKey)
        );

        assertEquals("Invalid API key.", exception.getMessage());
    }

    @Test
    void testGetWeatherDescription_UsesCacheIfDataExists() {
        String city = "Melbourne";
        String country = "AU";
        String description = "cloudy";

        CheckWeatherData cachedData = new CheckWeatherData();
        cachedData.setCity(city);
        cachedData.setCountry(country);
        cachedData.setDescription(description);

        when(weatherRepository.findByCityAndCountry(city, country)).thenReturn(Optional.of(cachedData));

        String result = checkWeatherService.getWeatherDescription(city, country, validApiKey);

        assertEquals(description, result);
        verify(weatherRepository, times(1)).findByCityAndCountry(city, country);
        verifyNoInteractions(restTemplate);  // Verify no HTTP call is made
    }

    @Test
    void testFetchAndCacheWeather_ReturnsAndCachesDescription() {
        String city = "Sydney";
        String country = "AU";
        String description = "clear sky";

        // Ensure no cached data exists
        when(weatherRepository.findByCityAndCountry(city, country)).thenReturn(Optional.empty());

        String result = checkWeatherService.getWeatherDescription(city, country, validApiKey);

        assertEquals(description, result);
        verify(weatherRepository, times(1)).save(any(CheckWeatherData.class));  // Verify caching behavior
        verify(restTemplate, times(1)).getForObject(anyString(), eq(String.class));  // Verify HTTP call was made
    }

    @Test
    void testParseDescription_FailsIfResponseInvalid() {
        String city = "Sydney";
        String country = "AU";

        // Mock RestTemplate to return an invalid JSON response
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("{\"invalid\": \"data\"}");

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> checkWeatherService.getWeatherDescription(city, country, validApiKey)
        );

        assertEquals("Failed to parse description from response", exception.getMessage());
    }
}
