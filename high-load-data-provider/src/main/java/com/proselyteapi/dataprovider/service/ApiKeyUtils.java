package com.proselyteapi.dataprovider.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("ApiKeyUtils")
public class ApiKeyUtils {

    public static final String API_KEY_HEADER = "x-api-key";

    private final String apiKey;

    public ApiKeyUtils(@Value("${data-provider.api-key}") String apiKey) {
        this.apiKey = Objects.requireNonNull(apiKey);
    }

    public boolean isValidApiKey(String checkApiKey) {
        return apiKey.equals(checkApiKey);
    }

    public String getApiKey() {
        return apiKey;
    }
}