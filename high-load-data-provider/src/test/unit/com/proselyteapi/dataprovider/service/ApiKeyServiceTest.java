package com.proselyteapi.dataprovider.service;

import annotation.Unit;
import com.proselyteapi.dataprovider.entity.ApiKey;
import com.proselyteapi.dataprovider.repository.ApiKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.LocalDateTime;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Unit
@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

    @Mock
    private ApiKeyRepository repository;

    private ApiKeyService apiKeyService;

    @BeforeEach
    void setup() {

        this.apiKeyService = new ApiKeyService(repository);
    }
    @Test
    void getApiKeyTest() {
        var username = "username";
        var apiKeyValue = "apiKey";
        var apiKey = ApiKey.builder()
            .apikey("apiKey")
            .username(username)
            .createdAt(LocalDateTime.now())
            .build();

        when(repository.findByUsername(username)).thenReturn(Mono.just(apiKey));

        var result = apiKeyService.getApiKey(username);

        StepVerifier.create(result)
            .expectNext(apiKeyValue)
            .expectComplete()
            .verify();

        verify(repository, times(1)).findByUsername(username);

    }
}