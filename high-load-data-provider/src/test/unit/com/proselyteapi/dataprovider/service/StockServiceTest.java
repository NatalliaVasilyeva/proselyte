package com.proselyteapi.dataprovider.service;

import annotation.Unit;
import com.proselyteapi.dataprovider.dto.StockRequestDto;
import com.proselyteapi.dataprovider.entity.Stock;
import com.proselyteapi.dataprovider.mapper.StockMapper;
import com.proselyteapi.dataprovider.repository.CustomStockRepository;
import com.proselyteapi.dataprovider.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveSetOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Unit
@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private ReactiveRedisTemplate<String, Stock> reactiveRedisStockTemplate;
    @Mock
    private ReactiveValueOperations<String, Stock> reactiveValueOperations;
    @Mock
    private ReactiveSetOperations<String, Stock> reactiveSetOperations;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private CustomStockRepository customStockRepository;

    private StockMapper stockMapper = StockMapper.MAPPER;
    private StockService stockService;

    @BeforeEach
    void setUp() {
        stockService = new StockService(reactiveRedisStockTemplate, stockRepository, customStockRepository);
    }
    @Test
    void createStockTest() {
        var stockRequestDto = StockRequestDto.builder()
            .symbol("aaaaa")
            .price(1000.0)
            .companyId(100L)
            .build();

        var stockToSave = stockMapper.map(stockRequestDto);

        var savedStock = Stock.builder()
            .id(1L)
            .symbol(stockRequestDto.getSymbol())
            .price(stockRequestDto.getPrice())
            .companyId(stockRequestDto.getCompanyId())
            .createdAt(LocalDateTime.now())
            .build();

        when(stockRepository.save(stockToSave)).thenReturn(Mono.just(savedStock));
        when(reactiveValueOperations.set(anyString(), any(Stock.class))).thenReturn(Mono.just(true));
        when(reactiveRedisStockTemplate.opsForValue()).thenReturn(reactiveValueOperations);

        var result = stockService.createStock(stockRequestDto);
        StepVerifier.create(result)
            .expectNext(stockMapper.map(savedStock))
            .expectComplete()
            .verify();

        verify(stockRepository, times(1)).save(stockToSave);
        verify(reactiveRedisStockTemplate, times(1)).opsForValue();
    }

    @Test
    void createStocksTest() {
        var stockRequestDtoOne = StockRequestDto.builder()
            .symbol("aaaaa")
            .price(1000.0)
            .companyId(100L)
            .build();

        var stockRequestDtoTwo = StockRequestDto.builder()
            .symbol("bbbb")
            .price(1001.0)
            .companyId(101L)
            .build();

        var stocksToSave = stockMapper.mapStockDtoList(List.of(stockRequestDtoOne, stockRequestDtoTwo));

        var savedStockOne = Stock.builder()
            .id(1L)
            .symbol(stockRequestDtoOne.getSymbol())
            .price(stockRequestDtoOne.getPrice())
            .companyId(stockRequestDtoOne.getCompanyId())
            .createdAt(LocalDateTime.now())
            .build();

        var savedStockTwo = Stock.builder()
            .id(2L)
            .symbol(stockRequestDtoTwo.getSymbol())
            .price(stockRequestDtoTwo.getPrice())
            .companyId(stockRequestDtoTwo.getCompanyId())
            .createdAt(LocalDateTime.now())
            .build();

        when(stockRepository.save(stocksToSave.get(0))).thenReturn(Mono.just(savedStockOne));
        when(stockRepository.save(stocksToSave.get(1))).thenReturn(Mono.just(savedStockTwo));
        when(reactiveValueOperations.set(anyString(), any(Stock.class))).thenReturn(Mono.just(true));
        when(reactiveRedisStockTemplate.opsForValue()).thenReturn(reactiveValueOperations);

        var result = stockService.createStocks(List.of(stockRequestDtoOne, stockRequestDtoTwo));

        StepVerifier.create(result)
            .recordWith(ArrayList::new)
            .expectNextCount(2)
            .consumeRecordedWith(c -> assertThat(c).containsExactlyElementsOf(List.of(stockMapper.map(savedStockOne), stockMapper.map(savedStockTwo))))
            .verifyComplete();

        verify(stockRepository, times(2)).save(any(Stock.class));
        verify(reactiveRedisStockTemplate, times(2)).opsForValue();
    }

    @Test
    void getAllStocksFromRedisTest() {
        String key = StockService.STOCK_KEY + "*";

        var stockOne = Stock.builder()
            .id(1L)
            .symbol("AAAA")
            .price(100.0)
            .companyId(1L)
            .createdAt(LocalDateTime.now())
            .build();

        var stockTwo = Stock.builder()
            .id(2L)
            .symbol("BBBB")
            .price(200.0)
            .companyId(2L)
            .createdAt(LocalDateTime.now())
            .build();

        when(reactiveRedisStockTemplate.keys(key)).thenReturn(Flux.just("stock:AAAA:" + stockOne.getCreatedAt().toString(), "stock:BBBB:" + stockTwo.getCreatedAt().toString()));
        when(reactiveValueOperations.get("stock:AAAA:" + stockOne.getCreatedAt().toString())).thenReturn(Mono.just(stockOne));
        when(reactiveValueOperations.get("stock:BBBB:" + stockTwo.getCreatedAt().toString())).thenReturn(Mono.just(stockTwo));
        when(reactiveRedisStockTemplate.opsForValue()).thenReturn(reactiveValueOperations);

        var result = stockService.getAllStocks();

        StepVerifier.create(result)
            .recordWith(ArrayList::new)
            .expectNextCount(2)
            .consumeRecordedWith(c -> assertThat(c).containsExactlyElementsOf(List.of(stockMapper.map(stockOne), stockMapper.map(stockTwo))))
            .verifyComplete();

        verify(stockRepository, times(0)).findAll();
        verify(reactiveRedisStockTemplate, times(2)).opsForValue();
        verify(reactiveRedisStockTemplate, times(1)).keys(anyString());
    }

    @Test
    void getAllStocksFromDBTest() {
        String key = StockService.STOCK_KEY + "*";

        var stockOne = Stock.builder()
            .id(1L)
            .symbol("AAAA")
            .price(100.0)
            .companyId(1L)
            .createdAt(LocalDateTime.now())
            .build();

        var stockTwo = Stock.builder()
            .id(2L)
            .symbol("BBBB")
            .price(200.0)
            .companyId(2L)
            .createdAt(LocalDateTime.now())
            .build();

        when(reactiveRedisStockTemplate.keys(key)).thenReturn(Flux.just("stock:CCCC:111"));
        when(reactiveValueOperations.get("stock:CCCC:111")).thenReturn(Mono.empty());
        when(reactiveValueOperations.set("stock:AAAA:" + stockOne.getCreatedAt().toString(), stockOne)).thenReturn(Mono.just(true));
        when(reactiveValueOperations.set("stock:BBBB:" + stockTwo.getCreatedAt().toString(), stockTwo)).thenReturn(Mono.just(true));
        when(reactiveRedisStockTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        when(stockRepository.findAll()).thenReturn(Flux.just(stockOne, stockTwo));

        var result = stockService.getAllStocks();

        StepVerifier.create(result)
            .recordWith(ArrayList::new)
            .expectNextCount(2)
            .consumeRecordedWith(c -> assertThat(c).containsExactlyElementsOf(List.of(stockMapper.map(stockOne), stockMapper.map(stockTwo))))
            .verifyComplete();

        verify(stockRepository, times(1)).findAll();
        verify(reactiveRedisStockTemplate, times(3)).opsForValue();
        verify(reactiveRedisStockTemplate, times(1)).keys(anyString());

    }

    @Test
    void getAllStocksBySymbolFromRedisTest() {
        String symbol = "AAAA";
        String key = StockService.STOCK_KEY + symbol;

        var stockOne = Stock.builder()
            .id(1L)
            .symbol(symbol)
            .price(100.0)
            .companyId(1L)
            .createdAt(LocalDateTime.now())
            .build();

        when(reactiveRedisStockTemplate.keys(key)).thenReturn(Flux.just(key));
        when(reactiveValueOperations.get(anyString())).thenReturn(Mono.just(stockOne));
        when(reactiveRedisStockTemplate.opsForValue()).thenReturn(reactiveValueOperations);

        var result = stockService.getAllBySymbol(symbol);

        StepVerifier.create(result)
            .recordWith(ArrayList::new)
            .expectNextCount(1)
            .consumeRecordedWith(c -> assertThat(c).containsAll(List.of(stockMapper.map(stockOne))))
            .verifyComplete();

        verify(stockRepository, times(0)).findAll();
        verify(reactiveRedisStockTemplate, times(1)).opsForValue();
        verify(reactiveRedisStockTemplate, times(1)).keys(anyString());
    }

    @Test
    void getAllStocksBySymbolFromDBTest() {
        String symbol = "AAAA";
        String key = StockService.STOCK_KEY + symbol;

        var stockOne = Stock.builder()
            .id(1L)
            .symbol(symbol)
            .price(100.0)
            .companyId(1L)
            .createdAt(LocalDateTime.now())
            .build();

        when(reactiveRedisStockTemplate.keys(key)).thenReturn(Flux.just("stock:CCCC"));
        when(reactiveValueOperations.get("stock:CCCC")).thenReturn(Mono.empty());
        when(reactiveValueOperations.set(key, stockOne)).thenReturn(Mono.just(true));
        when(reactiveRedisStockTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        when(stockRepository.findAllBySymbol(symbol)).thenReturn(Flux.just(stockOne));

        var result = stockService.getAllBySymbol(symbol);

        StepVerifier.create(result)
            .recordWith(ArrayList::new)
            .expectNextCount(1)
            .consumeRecordedWith(c -> assertThat(c).containsAll(List.of(stockMapper.map(stockOne))))
            .verifyComplete();

        verify(stockRepository, times(1)).findAllBySymbol(symbol);
        verify(reactiveRedisStockTemplate, times(2)).opsForValue();
        verify(reactiveRedisStockTemplate, times(1)).keys(anyString());
    }

    @Test
    void getLastChangedBySymbolFromRedisTest() {
        String symbol = "AAAA";
        String key = StockService.STOCK_KEY + symbol;

        var stockOne = Stock.builder()
            .id(1L)
            .symbol(symbol)
            .price(100.0)
            .companyId(1L)
            .createdAt(LocalDateTime.now().plusHours(1))
            .build();

        var stockTwo = Stock.builder()
            .id(1L)
            .symbol(symbol)
            .price(100.0)
            .companyId(1L)
            .createdAt(LocalDateTime.now().plusHours(2))
            .build();

        when(reactiveRedisStockTemplate.keys(key)).thenReturn(Flux.just("stock:AAAA:" + stockOne.getCreatedAt().toString(), "stock:AAAA:" + stockTwo.getCreatedAt().toString()));
        when(reactiveValueOperations.get("stock:AAAA:" + stockOne.getCreatedAt().toString())).thenReturn(Mono.just(stockOne));
        when(reactiveValueOperations.get("stock:AAAA:" + stockTwo.getCreatedAt().toString())).thenReturn(Mono.just(stockTwo));
        when(reactiveRedisStockTemplate.opsForValue()).thenReturn(reactiveValueOperations);

        var result = stockService.getLastChangedBySymbol(symbol);

        StepVerifier.create(result)
            .recordWith(ArrayList::new)
            .expectNextCount(1)
            .consumeRecordedWith(c -> {
                var answer = c.stream().toList();
                assertEquals(answer.get(0), stockMapper.map(stockTwo));
            })
            .verifyComplete();

        verify(stockRepository, times(0)).findFirstBySymbolOrderByCreatedAtDesc(symbol);
        verify(reactiveRedisStockTemplate, times(2)).opsForValue();
        verify(reactiveRedisStockTemplate, times(1)).keys(anyString());
    }

    @Test
    void getLastChangedBySymbolFromDBTest() {
        String symbol = "AAAA";
        String key = StockService.STOCK_KEY + symbol;

        var stockTwo = Stock.builder()
            .id(1L)
            .symbol(symbol)
            .price(100.0)
            .companyId(1L)
            .createdAt(LocalDateTime.now().plusHours(2))
            .build();

        when(reactiveRedisStockTemplate.keys(key)).thenReturn(Flux.just("stock:AAAA"));
        when(reactiveValueOperations.get("stock:AAAA")).thenReturn(Mono.empty());
        when(reactiveValueOperations.set(key, stockTwo)).thenReturn(Mono.just(true));
        when(reactiveRedisStockTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        when(stockRepository.findFirstBySymbolOrderByCreatedAtDesc(symbol)).thenReturn(Mono.just(stockTwo));


        var result = stockService.getLastChangedBySymbol(symbol);

        StepVerifier.create(result)
            .recordWith(ArrayList::new)
            .expectNextCount(1)
            .consumeRecordedWith(c -> {
                var answer = c.stream().toList();
                assertEquals(answer.get(0), stockMapper.map(stockTwo));
            })
            .verifyComplete();

        verify(stockRepository, times(1)).findFirstBySymbolOrderByCreatedAtDesc(symbol);
        verify(reactiveRedisStockTemplate, times(1)).keys(key);
        verify(reactiveRedisStockTemplate, times(2)).opsForValue();
        verify(reactiveValueOperations, times(1)).set(key, stockTwo);
    }

    @Test
    void deleteByIdTest() {
        Long id = 1L;
        String key = StockService.STOCK_KEY + id;


        var stockOne = Stock.builder()
            .id(id)
            .symbol("AAAA")
            .price(100.0)
            .companyId(1L)
            .createdAt(LocalDateTime.now().plusHours(1))
            .build();

        when(stockRepository.findById(id)).thenReturn(Mono.just(stockOne));
        when(stockRepository.delete(stockOne)).thenReturn(Mono.empty().then());
        when(reactiveRedisStockTemplate.delete(key)).thenReturn(Mono.just(1L));
//        when(reactiveValueOperations.delete(key)).thenReturn(Mono.just(true));
//        when(reactiveRedisStockTemplate.opsForValue()).thenReturn(reactiveValueOperations);

        var result = stockService.deleteById(id);
        StepVerifier.create(result)
            .verifyComplete();

        verify(stockRepository, times(1)).findById(id);
        verify(stockRepository, times(1)).delete(stockOne);
        verify(reactiveRedisStockTemplate, times(1)).delete(anyString());
    }

    @Test
    void deleteAllTest() {
        Long idOne = 1L;
        Long idTwo = 2L;
        String key = StockService.STOCK_KEY;


        var stockOne = Stock.builder()
            .id(idOne)
            .symbol("AAAA")
            .price(100.0)
            .companyId(1L)
            .createdAt(LocalDateTime.now().plusHours(1))
            .build();

        var stockTwo = Stock.builder()
            .id(idTwo)
            .symbol("BBBB")
            .price(100.0)
            .companyId(2L)
            .createdAt(LocalDateTime.now().plusHours(2))
            .build();

        when(stockRepository.findAll()).thenReturn(Flux.just(stockOne, stockTwo));
        when(stockRepository.deleteAll()).thenReturn(Mono.empty().then());
//        when(reactiveValueOperations.delete(key + "*")).thenReturn(Mono.just(true));
        when(reactiveRedisStockTemplate.keys(key + "*")).thenReturn(Flux.just("stock:AAAA", "stock:BBBB"));
        when(reactiveRedisStockTemplate.delete(any(Publisher.class))).thenReturn(Mono.just(1l));
//        when(reactiveRedisStockTemplate.opsForValue()).thenReturn(reactiveValueOperations);

        var result = stockService.deleteAll();
        StepVerifier.create(result)
            .verifyComplete();

        verify(stockRepository, times(1)).findAll();
        verify(stockRepository, times(1)).deleteAll();
        verify(reactiveRedisStockTemplate, times(1)).delete(any(Publisher.class));
    }


}