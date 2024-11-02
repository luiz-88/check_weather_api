package com.example.check_weather_api.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Setter
@Getter
@Table("check_weather_data")
public class CheckWeatherData {
    // Getter and Setter for id
    @Id
    private Long id;

    // Getter and Setter for city
    @Column("city")
    private String city;

    // Getter and Setter for country
    @Column("country")
    private String country;

    // Getter and Setter for description
    @Column("description")
    private String description;

}
