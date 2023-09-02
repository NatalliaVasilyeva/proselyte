package com.proselyteapi.dataprovider.generator;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@Getter
public class DataStorage {

    public static final Integer COMPANIES_NUMBER = 20;
    private final CopyOnWriteArrayList<String> symbols = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<String, Double> stocksPrice = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> companiesIds = new ConcurrentHashMap<>();
    private final BlockingQueue<String> companyNames = new LinkedBlockingQueue<>(COMPANIES_NUMBER);


    public String peekSymbol(Integer index) {
        return symbols.get(index);
    }

    public void addSymbol(String symbol) {
        symbols.addIfAbsent(symbol);
    }

    public Double getCurrentStockPrice(String symbol) {
        return stocksPrice.get(symbol);
    }

    public void updateCurrentStockPrice(String symbol, Double price) {
        stocksPrice.replace(symbol, price);
    }

    public void addCurrentStockPrice(String symbol, Double price) {
        stocksPrice.put(symbol, price);
    }

    public String getName() {
        return companyNames.poll();
    }

    public Long getCompanyId(String symbol) {
        return companiesIds.get(symbol);
    }

    public void addCompanyId(String symbol, Long id) {
        companiesIds.putIfAbsent(symbol, id);
    }

}
