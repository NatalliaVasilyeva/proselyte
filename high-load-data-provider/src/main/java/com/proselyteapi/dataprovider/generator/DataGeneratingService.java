package com.proselyteapi.dataprovider.generator;

import com.github.javafaker.Faker;
import com.proselyteapi.dataprovider.dto.CompanyRequestDto;
import com.proselyteapi.dataprovider.dto.StockRequestDto;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.proselyteapi.dataprovider.generator.DataStorage.COMPANIES_NUMBER;

@Service
@AllArgsConstructor
public class DataGeneratingService {

    private DataStorage dataStorage;
    private Faker faker;
    private Random random;

    private static final Double MIN_STOCK_PRICE = 100.0;
    private static final Double MAX_STOCK_PRICE = 2000.0;
    private static final Double MAX_PRICE_DIFFERENCE = 250.5;
    private static final List<String> OPERATORS = List.of("+", "-");

    @Autowired
    public DataGeneratingService(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        this.faker = new Faker();
        this.random = new Random();
    }

    public void fillSymbolsList() {
        Stream.iterate(0, n -> n < COMPANIES_NUMBER, n -> n + 1)
                .forEach(x -> dataStorage.addSymbol(createSymbol()));
    }

    private String createSymbol() {
        return UUID.randomUUID().toString().toUpperCase().substring(0, 5);
    }

    public void fillCompanyNamesQueue() {
        while (dataStorage.getCompanyNames().size() < COMPANIES_NUMBER) {
            dataStorage.addName(faker.company().name());
        }
    }

    public void fillCompanyIdsMap(String symbol, Long id) {
        dataStorage.addCompanyId(symbol, id);
    }

    public List<CompanyRequestDto> createCompanies() {
        List<CompletableFuture<CompanyRequestDto>> futures  = dataStorage.getAllSymbol().stream()
                .map(symbol ->
                        CompletableFuture.supplyAsync(() ->
                        CompanyRequestDto.builder()
                        .name(dataStorage.getName())
                        .symbol(symbol)
                        .enabled(true)
                        .build()))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList())
                .join();
    }

    public List<StockRequestDto> createStocks() {
        List<CompletableFuture<StockRequestDto>> futures =  Stream.iterate(0, n -> n < getRandomIntInRange(10, 40), n -> n + 1)
                .map(data -> CompletableFuture.supplyAsync(this::createStock))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList())
                .join();
    }

    public StockRequestDto createStock() {
        String symbol = dataStorage.peekSymbol(getRandomIntInRange(0, COMPANIES_NUMBER));
        return StockRequestDto.builder()
                .symbol(symbol)
                .price(createStockPrice(symbol))
                .companyId(dataStorage.getCompanyId(symbol))
                .build();
    }

    private Double createStockPrice(String symbol) {
        Double newPrice;
        Double currentPrice = dataStorage.getCurrentStockPrice(symbol);
        if (currentPrice == null) {
            newPrice = getRandomDoubleInRange(MIN_STOCK_PRICE, MAX_STOCK_PRICE);
            dataStorage.addCurrentStockPrice(symbol, newPrice);
            return newPrice;
        } else {
            String operator = OPERATORS.get(getRandomIntInRange(0, 1));
            newPrice = createUpdatingStockPrice(currentPrice, operator);
            dataStorage.updateCurrentStockPrice(symbol, newPrice);
        }
        return newPrice;
    }

    private Double createUpdatingStockPrice(Double currentPrice, String operator) {
        return switch (operator) {
            case "+" -> currentPrice + getRandomDoubleInRange(0.0, MAX_PRICE_DIFFERENCE);
            case "-" -> currentPrice - getRandomDoubleInRange(0.0, MAX_PRICE_DIFFERENCE);
            default -> currentPrice + getRandomDoubleInRange(0.0, MAX_PRICE_DIFFERENCE);
        };
    }

    private int getRandomIntInRange(int min, int max) {
        return random.nextInt(max - min) + min;
    }

    private Double getRandomDoubleInRange(Double min, Double max) {
        return random.nextDouble(max - min) + min;
    }

}
