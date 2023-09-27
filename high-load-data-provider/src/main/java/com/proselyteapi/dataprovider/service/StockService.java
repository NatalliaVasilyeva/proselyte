package com.proselyteapi.dataprovider.service;

import com.proselyteapi.dataprovider.dto.StockRequestDto;
import com.proselyteapi.dataprovider.dto.StockResponseDto;
import com.proselyteapi.dataprovider.entity.Stock;
import com.proselyteapi.dataprovider.mapper.StockMapper;
import com.proselyteapi.dataprovider.repository.CustomStockRepository;
import com.proselyteapi.dataprovider.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final ReactiveRedisTemplate<String, Stock> reactiveRedisStockTemplate;
    private final StockRepository stockRepository;
    private final CustomStockRepository customStockRepository;

    public static final String STOCK_KEY = "stock:";

    public Mono<StockResponseDto> createStock(StockRequestDto stockDto) {
        return Mono.just(stockDto)
            .map(StockMapper.MAPPER::map)
            .flatMap(stock -> stockRepository.save(stock)
                .flatMap(savedStock -> reactiveRedisStockTemplate.opsForValue().set(STOCK_KEY + savedStock.getSymbol() + savedStock.getCreatedAt().toString(), savedStock).thenReturn(savedStock)))
            .map(StockMapper.MAPPER::map);
    }

    public Flux<StockResponseDto> createStocks(List<StockRequestDto> stockDtos) {
        return Flux.fromIterable(stockDtos)
            .switchIfEmpty(Flux.empty())
            .map(StockMapper.MAPPER::map)
//            .flatMap(stocks -> customStockRepository.saveStock(stocks)
            .flatMap(stocks -> stockRepository.save(stocks)
                .flatMap(savedStock -> reactiveRedisStockTemplate.opsForValue().set(STOCK_KEY + savedStock.getSymbol() + ":" + savedStock.getCreatedAt().toString(), savedStock).thenReturn(savedStock)))
            .collectList()
            .map(StockMapper.MAPPER::mapStockList)
            .flatMapMany(Flux::fromIterable);
    }

    public Flux<StockResponseDto> getAllStocks() {
        return reactiveRedisStockTemplate.keys(STOCK_KEY + "*")
            // Fetching cached stocks.
            .flatMap(key -> reactiveRedisStockTemplate.opsForValue().get(key))
            // If cache is empty, fetch the database for stocks
            .switchIfEmpty(Flux.defer(() -> stockRepository.findAll()
                // Persisting the fetched stocks in the cache.
                .flatMap(stock -> reactiveRedisStockTemplate
                    .opsForValue()
                    .set(STOCK_KEY + stock.getSymbol() + ":" + stock.getCreatedAt().toString(), stock)
                    .thenReturn(stock)
                ))
//                // Fetching the stocks from the updated cache.
//                .thenMany(reactiveRedisStockTemplate
//                    .keys(STOCK_KEY + "*")
//                    .flatMap(key -> reactiveRedisStockTemplate.opsForValue().get(key))
//                ))
            )
            .collectList()
            .flatMapIterable(StockMapper.MAPPER::mapStockList);
    }

    public Flux<StockResponseDto> getAllBySymbol(String symbol) {

        return reactiveRedisStockTemplate.keys(STOCK_KEY + symbol)
            .flatMap(key -> reactiveRedisStockTemplate.opsForValue().get(key))
            .switchIfEmpty(Flux.defer(() -> stockRepository.findAllBySymbol(symbol)
                .flatMap(stock -> reactiveRedisStockTemplate
                    .opsForValue()
                    .set(STOCK_KEY + symbol, stock)
                    .thenReturn(stock)
                ))
            )
            .map(StockMapper.MAPPER::map);
    }

    public Mono<StockResponseDto> getLastChangedBySymbol(String symbol) {
        return reactiveRedisStockTemplate.opsForSet()
            .scan(STOCK_KEY + symbol)
            .switchIfEmpty(Mono.defer(() -> stockRepository.findFirstBySymbolOrderByCreatedAtDesc(symbol)
                .flatMap(stock -> reactiveRedisStockTemplate
                    .opsForValue()
                    .set(STOCK_KEY + symbol, stock)
                    .thenReturn(stock)
                ))
            )
            .sort(Comparator.comparing(Stock::getCreatedAt))
            .elementAt(0)
            .map(StockMapper.MAPPER::map);
    }

    public Mono<Void> deleteById(Long stockId) {
        return stockRepository.findById(stockId)
            .switchIfEmpty(Mono.defer(() -> Mono.error(new NoSuchElementException(String.format("Stock with id %s does not exist", stockId)))))
            .flatMap(stock -> stockRepository.delete(stock)
                .then(reactiveRedisStockTemplate.opsForValue().delete(STOCK_KEY + stock.getId())))
            .then();
    }

    public Mono<Void> deleteAll() {
        return stockRepository.findAll()
            .switchIfEmpty(Mono.defer(() -> Mono.error(new NoSuchElementException(String.format("No stocks exists")))))
            .then(stockRepository.deleteAll()
                .then(reactiveRedisStockTemplate.opsForValue().delete(STOCK_KEY + "*")))
            .then();
    }
}