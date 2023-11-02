package com.proselyteapi.stocksreceiver.mapper;

import annotation.Unit;
import com.proselyteapi.stocksreceiver.dto.StockDto;
import com.proselyteapi.stocksreceiver.entity.Stock;
import com.proselyteapi.stocksreceiver.mapper.StockMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Unit
class StockMapperTest {

    @Test
    void shouldReturnCorrectEntity() {
        var stockDto = new StockDto("aaaa",
            "test_name",
            java.math.BigDecimal.valueOf(100L),
            java.math.BigDecimal.valueOf(-10.29),
            java.math.BigDecimal.valueOf(0.0056),
            LocalDateTime.now().toInstant(ZoneOffset.UTC));

        var stock = StockMapper.MAPPER.map(stockDto);

        assertEquals(stockDto.symbol(), stock.getSymbol());
        assertEquals(stockDto.companyName(), stock.getCompanyName());
        assertEquals(stockDto.latestPrice(), stock.getLatestPrice());
        assertEquals(stockDto.change(), stock.getChange());
        assertEquals(stockDto.changePercent(), stock.getChangePercent());
        assertEquals(stockDto.latestUpdate(), stock.getLatestUpdate());
        assertThat(stock.getId()).isNull();
    }

    @Test
    void shouldReturnCorrectDto() {
        var stock = Stock.builder()
            .id(1L)
            .symbol("aaaaa")
            .companyName("test_name")
            .latestPrice(java.math.BigDecimal.valueOf(100L))
            .change(java.math.BigDecimal.valueOf(-10.29))
            .changePercent(java.math.BigDecimal.valueOf(0.0056))
            .latestUpdate(LocalDateTime.now().toInstant(ZoneOffset.UTC))
            .build();

        var stockDto = StockMapper.MAPPER.map(stock);

        assertEquals(stock.getSymbol(), stockDto.symbol());
        assertEquals(stock.getCompanyName(), stockDto.companyName());
        assertEquals(stock.getLatestPrice(), stockDto.latestPrice());
        assertEquals(stock.getChange(), stockDto.change());
        assertEquals(stock.getChangePercent(), stockDto.changePercent());
        assertEquals(stock.getLatestPrice(), stockDto.latestPrice());
    }
}