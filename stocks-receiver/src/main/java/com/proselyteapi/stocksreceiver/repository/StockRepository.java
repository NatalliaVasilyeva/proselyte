package com.proselyteapi.stocksreceiver.repository;

import com.proselyteapi.stocksreceiver.entity.Stock;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface StockRepository extends R2dbcRepository<Stock, Long> {

    @Query("""
        SELECT DISTINCT ON (symbol) symbol, company_name, latest_price, latest_update 
            FROM stock
            ORDER BY latest_price DESC, company_name ASC 
            LIMIT 5
        """)
    Flux<Stock> findTopFiveMostExpensiveStocks();

    @Query("""
        SELECT DISTINCT ON (symbol) symbol, company_name, latest_price, latest_update 
            FROM stock
            ORDER BY change_percent DESC, company_name ASC
            LIMIT 5
        """)
    Flux<Stock> findTopFiveMostChangedStocks();
}