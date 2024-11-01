package com.example.check_weather_api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("check_weather_data")
public class CheckWeatherData {

    @Id
    private Long id;

    @Column("city")
    private String city;

    @Column("country")
    private String country;

    @Column("description")
    private String description;
}
