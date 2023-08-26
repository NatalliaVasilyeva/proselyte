package com.proselyteapi.dataprovider.repository;

import com.proselyteapi.dataprovider.entity.Stock;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface StockRepository extends ReactiveCrudRepository<Stock, Long> {

    Mono<Stock> findFirstBySymbolOrderByCreatedAtDesc(String symbol);
    Flux<Stock> findAllBySymbol(String symbol);
}