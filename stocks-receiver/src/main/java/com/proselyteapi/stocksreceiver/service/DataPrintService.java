package com.proselyteapi.stocksreceiver.service;

import com.proselyteapi.stocksreceiver.entity.Stock;
import com.proselyteapi.stocksreceiver.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class DataPrintService {

    private final StockRepository stockRepository;

    public CompletableFuture<Void> printTopFiveExpensiveStocks() {
        return CompletableFuture.runAsync(() -> stockRepository.findTopFiveMostExpensiveStocks()
            .collectList()
            .doOnSuccess(stocks -> {
                log.info(printFormat(stocks, "Top five of most expensive stocks at this moment"));
            }));
    }

    public CompletableFuture<Void> printTopFiveMostChangedStocks() {
        return CompletableFuture.runAsync(() -> stockRepository.findTopFiveMostChangedStocks()
            .collectList()
            .doOnSuccess(stocks -> {
                log.info(printFormat(stocks, "Top five of most changed stocks at this moment"));
            }));
    }

    public void printStocksAnalytic() {

        stockRepository.findTopFiveMostExpensiveStocks()
            .collectList()
            .doOnSuccess(stocks -> {
                log.info(printFormat(stocks, "Top five of most expensive stocks at this moment"));
            })
            .doOnNext(next -> stockRepository.findTopFiveMostChangedStocks()
                .collectList()
                .doOnSuccess(stocks -> {
                    log.info(printFormat(stocks, "Top five of most changed stocks at this moment"));
                })
            )
            .subscribe();
    }

    private String printFormat(List<Stock> stocks, String capture) {
        StringBuilder sb = new StringBuilder();
        sb.append(capture)
            .append(System.lineSeparator())
            .append("---------------------------------------------------------------------------------------------")
            .append(System.lineSeparator())
            .append("SYMBOL").append("COMPANY NAME").append("LATEST PRICE").append("LATEST UPDATE")
            .append(System.lineSeparator())
            .append("---------------------------------------------------------------------------------------------");

        for (Stock stock : stocks) {
            sb.append(stock.getSymbol()).append(stock.getCompanyName()).append(stock.getLatestPrice()).append(stock.getLatestUpdate())
                .append(System.lineSeparator());
        }
        sb.append("---------------------------------------------------------------------------------------------");

        return sb.toString();
    }
}