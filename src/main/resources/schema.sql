CREATE TABLE IF NOT EXISTS check_weather_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    city VARCHAR(100),
    country VARCHAR(100),
    description VARCHAR(255)
);
