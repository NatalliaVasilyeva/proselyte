package com.proselyteapi.dataprovider.service;

import com.proselyteapi.dataprovider.dto.StockRequestDto;
import com.proselyteapi.dataprovider.dto.StockResponseDto;
import com.proselyteapi.dataprovider.mapper.StockMapper;
import com.proselyteapi.dataprovider.repository.CustomStockRepository;
import com.proselyteapi.dataprovider.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final CustomStockRepository customStockRepository;

    public Mono<StockResponseDto> createStock(StockRequestDto stockDto) {
        return Mono.just(stockDto)
                .map(StockMapper.MAPPER::map)
                .flatMap(stockRepository::save)
                .map(StockMapper.MAPPER::map);

    }
    public Flux<StockResponseDto> createStocks(List<StockRequestDto> stockDtos) {
        return Flux.fromIterable(stockDtos)
            .switchIfEmpty(Flux.empty())
            .flatMap(dto -> customStockRepository.saveStock(StockMapper.MAPPER.map(dto)))
            .collectList()
            .map(StockMapper.MAPPER::mapStockList)
            .flatMapMany(Flux::fromIterable);

//       return Flux.fromIterable(stockDtos)
//            .switchIfEmpty(Flux.empty())
//            .map(StockMapper.MAPPER.mapStockDtoList(stockDtos))
//            .flatMap(customStockRepository.saveStocks(StockMapper.MAPPER.mapStockDtoList(stockDtos)))
//            .map(StockMapper.MAPPER::mapStockList)
//            .flatMapMany(Flux::fromIterable);
    }


    public Flux<StockResponseDto> getAllStocks() {
        return stockRepository.findAll()
            .switchIfEmpty(Flux.empty())
            .collectList()
            .flatMapIterable(StockMapper.MAPPER::mapStockList);
    }

    public Flux<StockResponseDto> getAllBySymbol(String symbol) {

        return stockRepository.findAllBySymbol(symbol)
            .collectList()
            .map(StockMapper.MAPPER::mapStockList)
            .flatMapMany(Flux::fromIterable);
    }

    public Mono<StockResponseDto> getLastChangedBySymbol(String symbol) {
        return stockRepository.findFirstBySymbolOrderByCreatedAtDesc(symbol)
            .map(StockMapper.MAPPER::map);
    }
}