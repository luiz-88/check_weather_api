package com.example.check_weather_api.repository;

import com.example.check_weather_api.model.CheckWeatherData;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CheckWeatherRepository extends ReactiveCrudRepository<CheckWeatherData, Long> {
    Mono<CheckWeatherData> findByCityAndCountry(String city, String country);
}
