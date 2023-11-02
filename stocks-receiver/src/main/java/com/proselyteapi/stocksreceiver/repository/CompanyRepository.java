package com.proselyteapi.stocksreceiver.repository;

import com.proselyteapi.stocksreceiver.entity.Company;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;

public interface CompanyRepository extends R2dbcRepository<Company, Long> {

    @Modifying
    @Query(value = "INSERT INTO company (name, symbol, enabled) " +
        "VALUES (:#{#company.name},:#{#company.symbol},:#{#company.enabled}) " +
        "ON CONFLICT (symbol) DO UPDATE SET name=:#{#company.name}, enabled=:#{#company.enabled} " +
        "RETURNING *"
    )
    Mono<Company> upsert(@Param("company") Company company);

}