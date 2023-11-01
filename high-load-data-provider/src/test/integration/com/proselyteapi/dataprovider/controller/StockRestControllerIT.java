package com.proselyteapi.dataprovider.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.proselyteapi.dataprovider.dto.StockRequestDto;
import com.proselyteapi.dataprovider.dto.StockResponseDto;
import com.proselyteapi.dataprovider.entity.Company;
import com.proselyteapi.dataprovider.entity.Stock;
import com.proselyteapi.dataprovider.repository.CompanyRepository;
import com.proselyteapi.dataprovider.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StockRestControllerIT extends ApiBaseTest {

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private CompanyRepository companyRepository;

    private Company existingCompany;
    private Stock existingStock;

    @BeforeEach
    public void setup() {
        existingCompany = companyRepository.save(
                Company.builder()
                    .name("test_company")
                    .enabled(true)
                    .symbol("test")
                    .build())
            .block();

        existingStock = stockRepository.save(
            Stock.builder()
                .symbol("test")
                .price(1000.0)
                .isPrivilege(false)
                .companyId(existingCompany.getId())
                .createdAt(LocalDateTime.now().plusHours(1))
                .build())
            .block();
    }

    @AfterEach
    public void teardown() {
        companyRepository.deleteAll().subscribe();
        stockRepository.deleteAll().subscribe();
    }

    @Test
    @WithMockUser(username = "user", authorities = {"USER", "ADMIN"})
    void createStockTest() throws JsonProcessingException {

        var stockRequestDto = StockRequestDto.builder()
            .symbol("test")
            .price(1000.0)
            .companyId(existingCompany.getId())
            .build();

        final String jsonBody = objectMapper.writeValueAsString(stockRequestDto);


        webTestClient.post().uri("/api/v1/stock")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header("x-api-key", "aaa")
            .bodyValue(jsonBody)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(StockResponseDto.class)
            .consumeWith(response -> {
                var bodyStock = response.getResponseBody();
                assertEquals(stockRequestDto.getPrice(), bodyStock.getPrice());
                assertEquals(stockRequestDto.getSymbol(), bodyStock.getSymbol());
            });
    }

    @Test
    @WithMockUser(username = "user", authorities = {"USER", "ADMIN"})
    void createStocksTest() throws JsonProcessingException {

        var stockRequestDtoOne = StockRequestDto.builder()
            .symbol(existingCompany.getSymbol())
            .price(1000.0)
            .companyId(existingCompany.getId())
            .build();

        var stockRequestDtoTwo = StockRequestDto.builder()
            .symbol(existingCompany.getSymbol())
            .price(1001.0)
            .companyId(existingCompany.getId())
            .build();

        final String jsonBody = objectMapper.writeValueAsString(List.of(stockRequestDtoOne, stockRequestDtoTwo));


        webTestClient.post().uri("/api/v1/stocks")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header("x-api-key", "aaa")
            .bodyValue(jsonBody)
            .exchange()
            .expectStatus().isCreated()
            .expectBodyList(StockResponseDto.class)
            .hasSize(2)
            .consumeWith(response -> {
                List<StockResponseDto> stocks = response.getResponseBody();
                var stockOne = stocks.get(0);
                assertEquals(stockRequestDtoOne.getPrice(), stockOne.getPrice());
                assertEquals(stockRequestDtoOne.getSymbol(), stockOne.getSymbol());

                var stockTwo = stocks.get(1);
                assertEquals(stockRequestDtoTwo.getPrice(), stockTwo.getPrice());
                assertEquals(stockRequestDtoTwo.getSymbol(), stockTwo.getSymbol());
            });
    }

    @Test
    @WithMockUser(username = "user", authorities = {"USER", "ADMIN"})
    void getLastStockInfoBySymbolTest() {

        webTestClient.get().uri("/api/v1/stocks/" + existingStock.getSymbol() + "/quote")
            .accept(MediaType.APPLICATION_JSON)
            .header("x-api-key", "aaa")
            .exchange()
            .expectStatus().isOk()
            .expectBody(StockResponseDto.class)
            .consumeWith(response -> {
                var stockBody = response.getResponseBody();
                assertEquals(existingStock.getPrice(), stockBody.getPrice());
                assertEquals(existingStock.getSymbol(), stockBody.getSymbol());
            });
    }

    @Test
    @WithMockUser(username = "user", authorities = {"USER", "ADMIN"})
    void getAllStocksBySymbolTest() {

        webTestClient.get().uri("/api/v1/stocks/" + existingStock.getSymbol())
            .accept(MediaType.APPLICATION_JSON)
            .header("x-api-key", "aaa")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(StockResponseDto.class)
            .hasSize(1)
            .consumeWith(response -> {
                List<StockResponseDto> stocks = response.getResponseBody();
                var stock = stocks.get(0);
                assertEquals(existingStock.getPrice(), stock.getPrice());
                assertEquals(existingStock.getSymbol(), stock.getSymbol());
            });
    }
}