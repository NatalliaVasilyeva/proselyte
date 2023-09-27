package com.proselyteapi.dataprovider.repository;

import com.proselyteapi.dataprovider.entity.ApiKey;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface ApiKeyRepository extends R2dbcRepository<ApiKey, Long> {
    Mono<ApiKey> findByUsername(String username);

}