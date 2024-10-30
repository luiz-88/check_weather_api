package com.example.check_weather_api.repository;

import com.example.check_weather_api.model.CheckWeatherData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CheckWeatherRepository extends JpaRepository<CheckWeatherData, Long> {
    Optional<CheckWeatherData> findByCityAndCountry(String city, String country);
}
