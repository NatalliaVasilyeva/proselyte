package com.proselyteapi.stocksreceiver.service;

import annotation.Unit;
import com.proselyteapi.stocksreceiver.connector.CompanyConnector;
import com.proselyteapi.stocksreceiver.connector.StockConnector;
import com.proselyteapi.stocksreceiver.dto.CompanyDto;
import com.proselyteapi.stocksreceiver.dto.StockDto;
import com.proselyteapi.stocksreceiver.mapper.CompanyMapper;
import com.proselyteapi.stocksreceiver.mapper.StockMapper;
import com.proselyteapi.stocksreceiver.repository.CompanyRepository;
import com.proselyteapi.stocksreceiver.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Unit
@ExtendWith(MockitoExtension.class)
class DataCollectionServiceTest {

    private DataCollectionService dataCollectionService;
    @Mock
    private StockConnector stockConnector;
    @Mock
    private CompanyConnector companyConnector;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private StockRepository stockRepository;

    private StockMapper stockMapper = StockMapper.MAPPER;
    private CompanyMapper companyMapper = CompanyMapper.MAPPER;

    @BeforeEach
    void setUp() {
        dataCollectionService = new DataCollectionService(stockConnector, stockMapper, companyConnector, companyMapper, companyRepository, stockRepository);
        dataCollectionService.getSymbols().clear();
    }

    @Test
    void collectCompaniesDataTest() {
        var firstCompanyDto = new CompanyDto("AAAA", "Test_name_one", true);
        var firstCompany = companyMapper.map(firstCompanyDto);
        var secondCompanyDto = new CompanyDto("BBBB", "Test_name_two", false);

        when(companyConnector.getCompanies()).thenReturn(Flux.just(firstCompanyDto, secondCompanyDto));
        when(companyRepository.save(firstCompany)).thenReturn(Mono.just(firstCompany.toBuilder().id(1L).createdAt(LocalDateTime.now()).build()));

        var result = dataCollectionService.collectCompaniesData();

        assertThat(result).isNotNull();
        result.as(StepVerifier::create).verifyComplete();
        verify(companyConnector).getCompanies();
        verify(companyRepository).save(firstCompany);
        assertThat(dataCollectionService.getSymbols()).containsAll(List.of("AAAA"));
    }

    @Test
    void collectStocksDataTest() {
        var firstStockDto = new StockDto("AAAA", "Test_name_one", BigDecimal.valueOf(1000L), BigDecimal.valueOf(5L), BigDecimal.valueOf(0.015), LocalDateTime.now().toInstant(ZoneOffset.UTC));
        var firstStock = stockMapper.map(firstStockDto);
        var secondStockDto = new StockDto("BBBB", "Test_name_two", BigDecimal.valueOf(1010L), BigDecimal.valueOf(-100L), BigDecimal.valueOf(-0.035), LocalDateTime.now().minusDays(1).toInstant(ZoneOffset.UTC));
        var secondStock = stockMapper.map(secondStockDto);

        dataCollectionService.getSymbols().addAll(List.of("AAAA", "BBBB"));


        when(stockConnector.getStock("AAAA")).thenReturn(Mono.just(firstStockDto));
        when(stockConnector.getStock("BBBB")).thenReturn(Mono.just(secondStockDto));
        when(stockRepository.save(firstStock)).thenReturn(Mono.just(firstStock.toBuilder().id(1L).build()));
        when(stockRepository.save(secondStock)).thenReturn(Mono.just(secondStock.toBuilder().id(2L).build()));

        var result = dataCollectionService.collectStocksData();

        assertThat(result).isNotNull();
        result.as(StepVerifier::create).verifyComplete();
        verify(stockConnector).getStock("AAAA");
        verify(stockConnector).getStock("BBBB");
        verify(stockRepository).save(firstStock);
        verify(stockRepository).save(secondStock);
    }

}