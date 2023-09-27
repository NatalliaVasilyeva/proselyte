package com.proselyteapi.dataprovider.service;

import annotation.Unit;
import com.proselyteapi.dataprovider.dto.CompanyRequestDto;
import com.proselyteapi.dataprovider.entity.Company;
import com.proselyteapi.dataprovider.entity.Stock;
import com.proselyteapi.dataprovider.mapper.CompanyMapper;
import com.proselyteapi.dataprovider.repository.CompanyRepository;
import com.proselyteapi.dataprovider.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Unit
@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private ReactiveRedisTemplate<String, Company> reactiveRedisCompanyTemplate;
    @Mock
    private ReactiveRedisTemplate<String, Stock> reactiveRedisStockTemplate;
    @Mock
    private ReactiveValueOperations<String, Company> reactiveValueOperations;
    @Mock
    private ReactiveValueOperations<String, Stock> reactiveValueStockOperations;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private StockRepository stockRepository;

    private CompanyMapper companyMapper = CompanyMapper.MAPPER;
    private CompanyService companyService;

    @BeforeEach
    void setUp() {
        companyService = new CompanyService(reactiveRedisCompanyTemplate, reactiveRedisStockTemplate, companyRepository, stockRepository);
    }
    @Test
    void saveCompanyTest() {
        var companyRequestDto = CompanyRequestDto.builder()
            .name("A-company")
            .symbol("aaaaa")
            .enabled(true)
            .build();

        var companyToSave = companyMapper.map(companyRequestDto);

        var savedCompany = Company.builder()
            .id(1L)
            .name(companyRequestDto.getName())
            .symbol(companyRequestDto.getSymbol())
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(companyRepository.save(companyToSave)).thenReturn(Mono.just(savedCompany));
        when(reactiveValueOperations.set(anyString(), any(Company.class))).thenReturn(Mono.just(true));
        when(reactiveRedisCompanyTemplate.opsForValue()).thenReturn(reactiveValueOperations);

        var result = companyService.saveCompany(companyRequestDto);
        StepVerifier.create(result)
            .expectNext(companyMapper.map(savedCompany))
            .expectComplete()
            .verify();

        verify(companyRepository, times(1)).save(companyToSave);
        verify(reactiveRedisCompanyTemplate, times(1)).opsForValue();
    }

    @Test
    void saveCompaniesTest() {
        var companyRequestDtoOne = CompanyRequestDto.builder()
            .name("A-company")
            .symbol("aaaaa")
            .enabled(true)
            .build();

        var companyRequestDtoTwo = CompanyRequestDto.builder()
            .name("B-company")
            .symbol("bbbbb")
            .enabled(true)
            .build();

        var companiesToSave = companyMapper.mapCompanyDtoList(List.of(companyRequestDtoOne, companyRequestDtoTwo));

        var savedCompanyOne = Company.builder()
            .id(1L)
            .name(companyRequestDtoOne.getName())
            .symbol(companyRequestDtoOne.getSymbol())
            .enabled(companyRequestDtoOne.isEnabled())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        var savedCompanyTwo = Company.builder()
            .id(2L)
            .name(companyRequestDtoTwo.getName())
            .symbol(companyRequestDtoTwo.getSymbol())
            .enabled(companyRequestDtoTwo.isEnabled())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(companyRepository.save(companiesToSave.get(0))).thenReturn(Mono.just(savedCompanyOne));
        when(companyRepository.save(companiesToSave.get(1))).thenReturn(Mono.just(savedCompanyTwo));
        when(reactiveValueOperations.set(anyString(), any(Company.class))).thenReturn(Mono.just(true));
        when(reactiveRedisCompanyTemplate.opsForValue()).thenReturn(reactiveValueOperations);

        var result = companyService.saveCompanies(List.of(companyRequestDtoOne, companyRequestDtoTwo));

        StepVerifier.create(result)
            .recordWith(ArrayList::new)
            .expectNextCount(2)
            .consumeRecordedWith(c -> assertThat(c).containsExactlyElementsOf(List.of(companyMapper.map(savedCompanyOne), companyMapper.map(savedCompanyTwo))))
            .verifyComplete();

        verify(companyRepository, times(2)).save(any(Company.class));
        verify(reactiveRedisCompanyTemplate, times(2)).opsForValue();
    }

    @Test
    void getAllCompaniesWithoutStocksFromRedisTest() {
        String key = CompanyService.COMPANY_KEY + "*";

        var companyOne = Company.builder()
            .id(1L)
            .name("A-company")
            .symbol("aaaaa")
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        var companyTwo = Company.builder()
            .id(2L)
            .name("B-company")
            .symbol("bbbbb")
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(reactiveRedisCompanyTemplate.keys(key)).thenReturn(Flux.just("company:aaaaa", "company:bbbbb"));
        when(reactiveValueOperations.get("company:aaaaa")).thenReturn(Mono.just(companyOne));
        when(reactiveValueOperations.get("company:bbbbb")).thenReturn(Mono.just(companyTwo));
        when(reactiveRedisCompanyTemplate.opsForValue()).thenReturn(reactiveValueOperations);

        var result = companyService.getAllCompaniesWithoutStocks();

        StepVerifier.create(result)
            .recordWith(ArrayList::new)
            .expectNextCount(2)
            .consumeRecordedWith(c -> assertThat(c).containsExactlyElementsOf(List.of(companyMapper.map(companyOne), companyMapper.map(companyTwo))))
            .verifyComplete();

        verify(companyRepository, times(0)).findAll();
        verify(reactiveRedisCompanyTemplate, times(2)).opsForValue();
        verify(reactiveRedisCompanyTemplate, times(1)).keys(anyString());
    }

    @Test
    void getAllCompaniesWithoutStocksFromDBTest() {
        String key = CompanyService.COMPANY_KEY + "*";

        var companyOne = Company.builder()
            .id(1L)
            .name("A-company")
            .symbol("aaaaa")
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        var companyTwo = Company.builder()
            .id(2L)
            .name("B-company")
            .symbol("bbbbb")
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(reactiveRedisCompanyTemplate.keys(key)).thenReturn(Flux.just("company:CCCC"));
        when(reactiveValueOperations.get("company:CCCC")).thenReturn(Mono.empty());
        when(reactiveValueOperations.set("company:aaaaa", companyOne)).thenReturn(Mono.just(true));
        when(reactiveValueOperations.set("company:bbbbb", companyTwo)).thenReturn(Mono.just(true));
        when(reactiveRedisCompanyTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        when(companyRepository.findAll()).thenReturn(Flux.just(companyOne, companyTwo));

        var result = companyService.getAllCompaniesWithoutStocks();

        StepVerifier.create(result)
            .recordWith(ArrayList::new)
            .expectNextCount(2)
            .consumeRecordedWith(c -> assertThat(c).containsExactlyElementsOf(List.of(companyMapper.map(companyOne), companyMapper.map(companyTwo))))
            .verifyComplete();

        verify(companyRepository, times(1)).findAll();
        verify(reactiveRedisCompanyTemplate, times(3)).opsForValue();
        verify(reactiveRedisCompanyTemplate, times(1)).keys(anyString());

    }

    @Test
    void getCompanyBySymbolFromRedisTest() {
        String symbol = "AAAA";
        String key = CompanyService.COMPANY_KEY + symbol;

        var companyOne = Company.builder()
            .id(1L)
            .name("A-company")
            .symbol("aaaaa")
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(reactiveValueOperations.get(anyString())).thenReturn(Mono.just(companyOne));
        when(reactiveRedisCompanyTemplate.opsForValue()).thenReturn(reactiveValueOperations);

        var result = companyService.getBySymbol(symbol);

        StepVerifier.create(result)
            .recordWith(ArrayList::new)
            .expectNextCount(1)
            .consumeRecordedWith(c -> assertThat(c).containsAll(List.of(companyMapper.map(companyOne))))
            .verifyComplete();

        verify(companyRepository, times(0)).findBySymbol(symbol);
        verify(stockRepository, times(0)).findAllBySymbol(symbol);
        verify(reactiveRedisCompanyTemplate, times(1)).opsForValue();
    }

    @Test
    void getCompanyBySymbolFromDBTest() {
        String symbol = "AAAA";
        String key = CompanyService.COMPANY_KEY + symbol;

        var companyOne = Company.builder()
            .id(1L)
            .name("A-company")
            .symbol(symbol)
            .enabled(true)
            .stocks(List.of())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        var stockOne = Stock.builder()
            .id(1L)
            .symbol(symbol)
            .price(100.0)
            .companyId(1L)
            .createdAt(LocalDateTime.now().plusHours(1))
            .build();

        var stockTwo = Stock.builder()
            .id(2L)
            .symbol(symbol)
            .price(101.0)
            .companyId(1L)
            .createdAt(LocalDateTime.now().plusHours(2))
            .build();

        when(reactiveValueOperations.get(key)).thenReturn(Mono.empty());
        when(reactiveValueOperations.set(key, companyOne)).thenReturn(Mono.just(true));
        when(reactiveRedisCompanyTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        when(companyRepository.findBySymbol(symbol)).thenReturn(Mono.just(companyOne));
        when(stockRepository.findAllBySymbol(symbol)).thenReturn(Flux.just(stockOne, stockTwo));

        var result = companyService.getBySymbol(symbol);

        StepVerifier.create(result)
            .recordWith(ArrayList::new)
            .expectNextCount(1)
            .consumeRecordedWith(c -> assertThat(c).containsAll(List.of(companyMapper.map(companyOne))))
            .verifyComplete();

        verify(companyRepository, times(1)).findBySymbol(symbol);
        verify(stockRepository, times(1)).findAllBySymbol(symbol);
        verify(reactiveRedisCompanyTemplate, times(2)).opsForValue();
    }

    @Test
    void deleteByIdTest() {
        Long id = 1L;
        String symbol = "aaaaa";
        String stockKey = StockService.STOCK_KEY + id;
        String companyKey = CompanyService.COMPANY_KEY + id;

        var companyOne = Company.builder()
            .id(id)
            .name("A-company")
            .symbol(symbol)
            .enabled(true)
            .stocks(List.of())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        var stockOne = Stock.builder()
            .id(id)
            .symbol(symbol)
            .price(100.0)
            .companyId(id)
            .createdAt(LocalDateTime.now().plusHours(1))
            .build();

        when(companyRepository.findById(id)).thenReturn(Mono.just(companyOne));
        when(stockRepository.findAllBySymbol(symbol)).thenReturn(Flux.just(stockOne));
        when(companyRepository.delete(companyOne)).thenReturn(Mono.empty().then());
        when(stockRepository.delete(stockOne)).thenReturn(Mono.empty().then());
        when(reactiveValueStockOperations.delete(stockKey)).thenReturn(Mono.just(true));
        when(reactiveValueOperations.delete(companyKey)).thenReturn(Mono.just(true));
        when(reactiveRedisCompanyTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        when(reactiveRedisStockTemplate.opsForValue()).thenReturn(reactiveValueStockOperations);

        var result = companyService.deleteById(id);
        StepVerifier.create(result)
            .verifyComplete();

        verify(companyRepository, times(1)).findById(id);
        verify(stockRepository, times(1)).findAllBySymbol(symbol);
        verify(companyRepository, times(1)).delete(companyOne);
        verify(stockRepository, times(1)).delete(stockOne);
        verify(reactiveRedisStockTemplate, times(1)).opsForValue();
        verify(reactiveRedisCompanyTemplate, times(1)).opsForValue();
    }

    @Test
    void deleteAllTest() {
        Long idOne = 1L;
        Long idTwo = 2L;
        String stockKey = StockService.STOCK_KEY;
        String companyKey = CompanyService.COMPANY_KEY;


        var companyOne = Company.builder()
            .id(idOne)
            .name("A-company")
            .symbol("aaaaa")
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        var companyTwo = Company.builder()
            .id(idTwo)
            .name("B-company")
            .symbol("bbbbb")
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        var stockOne = Stock.builder()
            .id(idOne)
            .symbol(companyOne.getSymbol())
            .price(100.0)
            .companyId(idOne)
            .createdAt(LocalDateTime.now().plusHours(1))
            .build();

        var stockTwo = Stock.builder()
            .id(idTwo)
            .symbol(companyTwo.getSymbol())
            .price(101.0)
            .companyId(idTwo)
            .createdAt(LocalDateTime.now().plusHours(2))
            .build();

        when(companyRepository.findAll()).thenReturn(Flux.just(companyOne, companyTwo));
        when(companyRepository.delete(companyOne)).thenReturn(Mono.empty().then());
        when(companyRepository.delete(companyTwo)).thenReturn(Mono.empty().then());
        when(stockRepository.findAllBySymbol(companyOne.getSymbol())).thenReturn(Flux.just(stockOne));
        when(stockRepository.findAllBySymbol(companyTwo.getSymbol())).thenReturn(Flux.just(stockTwo));
        when(stockRepository.delete(stockOne)).thenReturn(Mono.empty().then());
        when(stockRepository.delete(stockTwo)).thenReturn(Mono.empty().then());
        when(reactiveValueStockOperations.delete(anyString())).thenReturn(Mono.just(true));
        when(reactiveValueOperations.delete(anyString())).thenReturn(Mono.just(true));
        when(reactiveRedisCompanyTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        when(reactiveRedisStockTemplate.opsForValue()).thenReturn(reactiveValueStockOperations);

        var result = companyService.deleteAll();
        StepVerifier.create(result)
            .verifyComplete();

        verify(companyRepository, times(1)).findAll();
        verify(companyRepository, times(2)).delete(any(Company.class));
        verify(stockRepository, times(2)).findAllBySymbol(anyString());
        verify(stockRepository, times(2)).delete(any(Stock.class));
        verify(reactiveRedisStockTemplate, times(2)).opsForValue();
        verify(reactiveRedisCompanyTemplate, times(2)).opsForValue();
    }
}