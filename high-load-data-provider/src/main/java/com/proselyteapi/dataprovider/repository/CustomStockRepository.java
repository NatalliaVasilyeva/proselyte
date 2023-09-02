package com.proselyteapi.dataprovider.repository;

import com.proselyteapi.dataprovider.entity.Stock;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@AllArgsConstructor
public class CustomStockRepository {
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    public Flux<Stock> saveStocks(List<Stock> stocks) {
        return Flux.fromIterable(stocks)
            .filter(Objects::nonNull)
            .flatMap(this::saveStock)
            .doOnNext(st -> log.debug("{} stocks were saved", stocks.size()));
    }

    public Mono<Stock> saveStock(Stock stock) {
        return r2dbcEntityTemplate.insert(stock)
            .doOnError(throwable -> log.error("Stock: {} was not saved due to an error: {}", stock, throwable.getMessage()))
            .doOnNext(st -> log.debug("Stock was saved"));
    }
}