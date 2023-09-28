package com.proselyteapi.dataprovider.service;

import com.proselyteapi.dataprovider.entity.ApiKey;
import com.proselyteapi.dataprovider.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository repository;

    public Mono<String> getApiKey(String username) {
        return repository.findByUsername(username)
            .map(ApiKey::getApikey);

    }
}