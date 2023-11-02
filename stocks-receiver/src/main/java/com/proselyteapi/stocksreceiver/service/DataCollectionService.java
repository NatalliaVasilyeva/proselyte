package com.proselyteapi.stocksreceiver.service;

import com.proselyteapi.stocksreceiver.connector.CompanyConnector;
import com.proselyteapi.stocksreceiver.connector.StockConnector;
import com.proselyteapi.stocksreceiver.dto.CompanyDto;
import com.proselyteapi.stocksreceiver.mapper.CompanyMapper;
import com.proselyteapi.stocksreceiver.mapper.StockMapper;
import com.proselyteapi.stocksreceiver.repository.CompanyRepository;
import com.proselyteapi.stocksreceiver.repository.StockRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
@Service
public class DataCollectionService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final StockConnector stockConnector;
    private final StockMapper stockMapper;
    private final CompanyConnector companyConnector;
    private final CompanyMapper companyMapper;
    private final CompanyRepository companyRepository;
    private final StockRepository stockRepository;

    @Getter
    private final Set<String> symbols = new LinkedHashSet<>();

    public Mono<Void> collectCompaniesData() {
       companyConnector.getCompanies()
            .onErrorContinue((e, o) -> LOG.error("Error appeared while get companies data", e.getMessage()))
            .filter(CompanyDto::isEnabled)
            .map(companyMapper::map)
            .map(company -> {
               symbols.add(company.getSymbol());
                return company;
            })
            .map(companyRepository::save)
            .subscribe();
        return Mono.empty();
    }

    public Mono<Void> collectStocksData() {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        Flux.fromIterable(symbols)
            .map(symbol -> CompletableFuture.supplyAsync(() -> stockConnector.getStock(symbol)
                .onErrorContinue((e, o) -> LOG.error("Error appeared while get stock data", e.getMessage())), executor))
            .collect(collectingAndThen(toList(), list -> allOf(list).join()))
            .flatMapMany(Flux::fromIterable)
            .flatMap(Function.identity())
            .map(stockMapper::map)
            .map(stockRepository::save)
            .subscribe();
        return Mono.empty();
    }


    private <T> CompletableFuture<List<T>> allOf(List<CompletableFuture<T>> futuresList) {
        return CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[0]))
            .thenApply(v ->
                futuresList.stream().
                    map(CompletableFuture::join)
                    .toList()
            );
    }
}