package com.proselyteapi.dataprovider.service;

import com.proselyteapi.dataprovider.dto.StockRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


class StockServiceIT extends BaseServiceTest {

    @Autowired
    private StockService stockService;

    @AfterEach
    public void teardown() {
        stockService.deleteAll().subscribe();
    }

    @Test
    void shouldSaveStockTest() {

        var stockRequestDto = StockRequestDto.builder()
            .price(1000.90)
            .symbol("test")
            .companyId(1l)
            .build();

        stockService.createStock(stockRequestDto)
            .doOnNext(result -> {
                assertEquals(stockRequestDto.getSymbol(), result.getSymbol());
                assertEquals(stockRequestDto.getPrice(), result.getPrice());
            });
    }

    @Test
    void shouldSaveStocksTest() {

        var stockRequestDtoOne = StockRequestDto.builder()
            .price(1000.90)
            .symbol("test")
            .companyId(1l)
            .build();

        var stockRequestDtoTwo = StockRequestDto.builder()
            .price(1000.0)
            .symbol("testt")
            .companyId(2l)
            .build();

        var stocks = stockService.createStocks(List.of(stockRequestDtoOne, stockRequestDtoTwo)).collectList().block();

        assertThat(stocks).hasSize(2);

        var stockOne = stocks.get(0);
        assertEquals(stockRequestDtoOne.getSymbol(), stockOne.getSymbol());
        assertEquals(stockRequestDtoOne.getPrice(), stockOne.getPrice());

        var stockTwo = stocks.get(1);
        assertEquals(stockRequestDtoTwo.getSymbol(), stockTwo.getSymbol());
        assertEquals(stockRequestDtoTwo.getPrice(), stockTwo.getPrice());
    }
}