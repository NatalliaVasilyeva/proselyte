package com.proselyteapi.dataprovider.service;

import com.proselyteapi.dataprovider.dto.StockRequestDto;
import com.proselyteapi.dataprovider.dto.StockResponseDto;
import com.proselyteapi.dataprovider.mapper.StockMapper;
import com.proselyteapi.dataprovider.repository.CustomStockRepository;
import com.proselyteapi.dataprovider.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
//@CacheConfig(cacheNames = "stocks")
@ConditionalOnProperty(name = "cache.enabled", havingValue = "true")
public class StockService {

    private final StockRepository stockRepository;
    private final CustomStockRepository customStockRepository;

//    @CachePut(cacheNames = "stocks", key = "#result.block().symbol + #result.block().createdAt", unless = "#result.block().symbol==null")
    public Mono<StockResponseDto> createStock(StockRequestDto stockDto) {
        return Mono.just(stockDto)
                .map(StockMapper.MAPPER::map)
                .flatMap(stockRepository::save)
                .map(StockMapper.MAPPER::map);

    }

//    @CachePut(cacheNames = "stocks", key = "#result.block().symbol + #result.block().createdAt", unless = "#result.block().symbol==null")
    public Flux<StockResponseDto> createStocks(List<StockRequestDto> stockDtos) {
        return Flux.fromIterable(stockDtos)
                .switchIfEmpty(Flux.empty())
                .flatMap(dto -> customStockRepository.saveStock(StockMapper.MAPPER.map(dto)))
                .collectList()
                .map(StockMapper.MAPPER::mapStockList)
                .flatMapMany(Flux::fromIterable);
    }

//    @Cacheable(cacheNames = "stocks")
    public Flux<StockResponseDto> getAllStocks() {
        return stockRepository.findAll()
                .switchIfEmpty(Flux.empty())
                .collectList()
                .flatMapIterable(StockMapper.MAPPER::mapStockList)
                .cache();
    }

//    @Cacheable(cacheNames = "stocks")
    public Flux<StockResponseDto> getAllBySymbol(String symbol) {

        return stockRepository.findAllBySymbol(symbol)
                .collectList()
                .map(StockMapper.MAPPER::mapStockList)
                .flatMapMany(Flux::fromIterable)
                .cache();
    }

//    @Cacheable(cacheNames = "stocks", key = "#result.block().symbol + #result.block().createdAt")
    public Mono<StockResponseDto> getLastChangedBySymbol(String symbol) {
        return stockRepository.findFirstBySymbolOrderByCreatedAtDesc(symbol)
                .map(StockMapper.MAPPER::map)
                .cache();
    }

    /* Clears cache after 10 minutes. */
//    @CacheEvict(allEntries = true, cacheNames = {"stocks"})
//    @Scheduled(fixedDelay = 600000)
//    public void cacheEvict() {
//        log.info("Cleaning cache stocks");
//    }
}