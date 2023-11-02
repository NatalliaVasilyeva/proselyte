package com.proselyteapi.stocksreceiver.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record StockDto(String symbol, String companyName, BigDecimal latestPrice, BigDecimal change, BigDecimal changePercent, Instant latestUpdate) {
}