package com.proselyteapi.stocksreceiver.service;

import annotation.Unit;
import com.proselyteapi.stocksreceiver.entity.Stock;
import com.proselyteapi.stocksreceiver.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Unit
@ExtendWith(MockitoExtension.class)
class DataPrintServiceTest {

    private DataPrintService dataPrintService;
    @Mock
    private StockRepository stockRepository;

    @BeforeEach
    void setUp() {
        dataPrintService = new DataPrintService(stockRepository);
    }

    @Test
    void shouldPrintTopFiveExpensiveStocksTest() throws ExecutionException, InterruptedException {
        var firstStock = new Stock().toBuilder().id(1L).symbol("AAAA").latestPrice(BigDecimal.valueOf(1000L)).change(BigDecimal.valueOf(5L)).changePercent(BigDecimal.valueOf(0.015)).latestUpdate(LocalDateTime.now().toInstant(ZoneOffset.UTC)).build();
        var secondStock = new Stock().toBuilder().id(2L).symbol("BBBB").latestPrice(BigDecimal.valueOf(1010L)).change(BigDecimal.valueOf(-100L)).changePercent(BigDecimal.valueOf(-0.015)).latestUpdate(LocalDateTime.now().toInstant(ZoneOffset.UTC)).build();
        when(stockRepository.findTopFiveMostExpensiveStocks()).thenReturn(Flux.just(firstStock, secondStock));

        dataPrintService.printTopFiveExpensiveStocks().join();

        verify(stockRepository).findTopFiveMostExpensiveStocks();
    }

    @Test
    void shouldPrintTopFiveMostChangedStocksTest() {
        var firstStock = new Stock().toBuilder().id(1L).symbol("AAAA").latestPrice(BigDecimal.valueOf(1000L)).change(BigDecimal.valueOf(5L)).changePercent(BigDecimal.valueOf(0.015)).latestUpdate(LocalDateTime.now().toInstant(ZoneOffset.UTC)).build();
        var secondStock = new Stock().toBuilder().id(2L).symbol("BBBB").latestPrice(BigDecimal.valueOf(1010L)).change(BigDecimal.valueOf(-100L)).changePercent(BigDecimal.valueOf(-0.015)).latestUpdate(LocalDateTime.now().toInstant(ZoneOffset.UTC)).build();
        when(stockRepository.findTopFiveMostChangedStocks()).thenReturn(Flux.just(firstStock, secondStock));

        dataPrintService.printTopFiveMostChangedStocks().join();

        verify(stockRepository).findTopFiveMostChangedStocks();
    }

}