CREATE TABLE IF NOT EXISTS check_weather_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    city VARCHAR(255),
    country VARCHAR(255),
    description VARCHAR(255)
);
