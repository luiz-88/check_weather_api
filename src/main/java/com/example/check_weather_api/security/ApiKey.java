package com.example.check_weather_api.security;
public enum ApiKey {
    API_KEY_1("API_KEY_1"),
    API_KEY_2("API_KEY_2"),
    API_KEY_3("API_KEY_3"),
    API_KEY_4("API_KEY_4"),
    API_KEY_5("API_KEY_5");

    private final String envVariableName;

    ApiKey(String envVariableName) {
        this.envVariableName = envVariableName;
    }

    /**
     * Retrieve the API key's value from environment variables.
     *
     * @return the value of the API key from the environment, or throws an exception if not set.
     */
    public String getValue() {
        String value = System.getenv(envVariableName);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException("API key for " + envVariableName + " is not set in environment variables.");
        }
        return value;
    }
}

