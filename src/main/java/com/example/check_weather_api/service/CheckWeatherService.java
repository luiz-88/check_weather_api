package com.example.check_weather_api.service;

import com.example.check_weather_api.model.CheckWeatherData;
import com.example.check_weather_api.model.CheckWeatherResponse;
import com.example.check_weather_api.repository.CheckWeatherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CheckWeatherService {
    private final CheckWeatherRepository weatherRepository;
    private final WebClient webClient;
    private static final Logger logger = LoggerFactory.getLogger(CheckWeatherService.class);


    @Autowired
    public CheckWeatherService(WebClient.Builder webClientBuilder,
                               CheckWeatherRepository weatherRepository,
                                @Value("${openweathermap.base.url}") String baseUrl){
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.weatherRepository = weatherRepository;
    }

    @Cacheable(cacheNames = "weather", key = "#city + ',' + #country ")
    public Mono<String> getWeatherDescription(String city, String country, String clientApiKey) {
        return weatherRepository.findByCityAndCountry(city, country)
                .flatMap(data -> Mono.just(data.getDescription()))
                .switchIfEmpty(fetchAndCacheWeatherData(city, country, clientApiKey));
    }

    private Mono<String> fetchAndCacheWeatherData(String city, String country, String clientApiKey) {
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

                    // Save the description asynchronously in H2
                    return saveWeatherData(city, country, description).thenReturn(description);
                });
    }
    @Transactional
    private Mono<Void> saveWeatherData(String city, String country, String description) {
        logger.debug("Saving weather data for city: {}, country: {}, description: {}", city, country, description);
        CheckWeatherData data = new CheckWeatherData();
        data.setCity(city);
        data.setCountry(country);
        data.setDescription(description);
        return weatherRepository.save(data).then();
    }
}