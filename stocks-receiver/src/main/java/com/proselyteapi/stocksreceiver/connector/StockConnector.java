package com.proselyteapi.stocksreceiver.connector;

import com.proselyteapi.stocksreceiver.dto.StockDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Component
public class StockConnector {

    private WebClient webClient;

    @Value("${service.source.token}")
    private String token;


    public Mono<StockDto> getStock(String symbol) {
        return requestStock("quote", symbol);
    }

    public Flux<StockDto> getStocks(List<String> symbols) {
        return requestStocks("quote", symbols);
    }

    private Mono<StockDto> requestStock(String pathSegment, String symbol) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder.pathSegment(pathSegment)
                .pathSegment(symbol)
                .queryParam("token", token)
                .build())
            .retrieve()
            .bodyToMono(StockDto.class);
    }

    private Flux<StockDto> requestStocks(String pathSegment, List<String> symbols) {
        String symbolsString = String.join(".", symbols);
        return webClient.get()
            .uri(uriBuilder -> uriBuilder.pathSegment(pathSegment)
                .pathSegment(symbolsString)
                .queryParam("token", token)
                .build())
            .retrieve()
            .bodyToFlux(StockDto.class);
    }
}