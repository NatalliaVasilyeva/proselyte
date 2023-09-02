package com.proselyteapi.dataprovider.repository;

import com.proselyteapi.dataprovider.entity.Company;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CompanyRepository extends ReactiveCrudRepository<Company, Long> {
    Mono<Company> findByName(String name);
    Mono<Company> findBySymbol(String symbol);
}