package com.example.check_weather_api.service;

import com.example.check_weather_api.configuration.ApiKeyConfig;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.mockito.Mockito.when;

class CheckWeatherServiceTest {

    @Mock
    private ApiKeyConfig apiKeyConfig;

    @InjectMocks
    private CheckWeatherService checkWeatherService;

    private final String validApiKey = "valid-api-key";
    private final String invalidApiKey = "invalid-api-key";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(apiKeyConfig.getKeys()).thenReturn(Collections.singletonList(validApiKey));
        checkWeatherServ
