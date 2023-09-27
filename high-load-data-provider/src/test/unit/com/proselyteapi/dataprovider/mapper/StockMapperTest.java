package com.proselyteapi.dataprovider.mapper;

import annotation.Unit;
import com.proselyteapi.dataprovider.dto.StockRequestDto;
import com.proselyteapi.dataprovider.entity.Stock;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Unit
class StockMapperTest {

    @Test
    void shouldReturnCorrectEntity() {
        var stockDto = StockRequestDto.builder()
            .id(1L)
            .symbol("aaaaa")
            .price(100.0)
            .companyId(100L)
            .build();

        var stock = StockMapper.MAPPER.map(stockDto);

        assertEquals(stockDto.getSymbol(), stock.getSymbol());
        assertEquals(stockDto.getPrice(), stock.getPrice());
        assertEquals(stockDto.getCompanyId(), stock.getCompanyId());
        assertFalse(stock.isPrivilege());
        assertThat(stock.getCreatedAt()).isNull();
        assertThat(stock.getId()).isNull();
    }

    @Test
    void shouldReturnCorrectEntities() {
        var stockDtoFirst = StockRequestDto.builder()
            .id(1L)
            .symbol("aaaaa")
            .price(100.0)
            .companyId(100L)
            .build();

        var stockDtoSecond = StockRequestDto.builder()
            .id(2L)
            .symbol("bbbbb")
            .price(100.0)
            .companyId(101L)
            .build();

        var stocks = StockMapper.MAPPER.mapStockDtoList(List.of(stockDtoFirst, stockDtoSecond));

        assertThat(stocks).hasSize(2);
        var stockOne = stocks.get(0);
        assertEquals(stockDtoFirst.getSymbol(), stockOne.getSymbol());
        assertEquals(stockDtoFirst.getPrice(), stockOne.getPrice());
        assertEquals(stockDtoFirst.getCompanyId(), stockOne.getCompanyId());
        assertFalse(stockOne.isPrivilege());
        assertThat(stockOne.getCreatedAt()).isNull();
        assertThat(stockOne.getId()).isNull();

        var stockTwo = stocks.get(1);
        assertEquals(stockDtoSecond.getSymbol(), stockTwo.getSymbol());
        assertEquals(stockDtoSecond.getPrice(), stockTwo.getPrice());
        assertEquals(stockDtoSecond.getCompanyId(), stockTwo.getCompanyId());
        assertFalse(stockTwo.isPrivilege());
        assertThat(stockTwo.getCreatedAt()).isNull();
        assertThat(stockTwo.getId()).isNull();
    }

    @Test
    void shouldReturnCorrectDto() {
        var stock = Stock.builder()
            .id(1L)
            .symbol("aaaaa")
            .price(100.0)
            .isPrivilege(false)
            .companyId(100L)
            .createdAt(LocalDateTime.now())
            .build();

        var stockResponseDto = StockMapper.MAPPER.map(stock);

        assertEquals(stock.getId(), stockResponseDto.getId());
        assertEquals(stock.getSymbol(), stockResponseDto.getSymbol());
        assertEquals(stock.getPrice(), stockResponseDto.getPrice());
    }

    @Test
    void shouldReturnCorrectDtos() {
        var stockOne = Stock.builder()
            .id(1L)
            .symbol("aaaaa")
            .price(100.0)
            .isPrivilege(false)
            .companyId(100L)
            .createdAt(LocalDateTime.now())
            .build();

        var stockTwo = Stock.builder()
            .id(2L)
            .symbol("bbbbb")
            .price(100.0)
            .isPrivilege(false)
            .companyId(101L)
            .createdAt(LocalDateTime.now())
            .build();

        var stockResponseDtos = StockMapper.MAPPER.mapStockList(List.of(stockOne, stockTwo));

        assertThat(stockResponseDtos).hasSize(2);
        var stockResponseDtoOne = stockResponseDtos.get(0);
        assertEquals(stockOne.getId(), stockResponseDtoOne.getId());
        assertEquals(stockOne.getSymbol(), stockResponseDtoOne.getSymbol());
        assertEquals(stockOne.getPrice(), stockResponseDtoOne.getPrice());

        var stockResponseDtoTwo = stockResponseDtos.get(1);
        assertEquals(stockTwo.getId(), stockResponseDtoTwo.getId());
        assertEquals(stockTwo.getSymbol(), stockResponseDtoTwo.getSymbol());
        assertEquals(stockTwo.getPrice(), stockResponseDtoTwo.getPrice());
    }

}