package com.proselyteapi.dataprovider.controller;

import com.proselyteapi.dataprovider.dto.StockRequestDto;
import com.proselyteapi.dataprovider.dto.StockResponseDto;
import com.proselyteapi.dataprovider.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Tag(name = "Stock API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Validated
public class StockRestController {

    private final StockService stockService;

    public static final String API_KEY_HEADER = "x-api-key";

    @PostMapping("/stock")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create stock")
    public Mono<StockResponseDto> createStock(@RequestHeader(name = API_KEY_HEADER) @Valid String apiKey,
                                                  @Parameter(required = true) @Valid @RequestBody StockRequestDto stockDto) {
        return stockService.createStock(stockDto);
    }

    @PostMapping("/stocks")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create stocks")
    public Flux<StockResponseDto> createStocks(@RequestHeader(name = API_KEY_HEADER) @Valid String apiKey,
                                               @Parameter(required = true) @Valid @RequestBody List<StockRequestDto> stockDtos) {
        return stockService.createStocks(stockDtos);
    }

    @GetMapping("/stocks/{stock_code}/quote")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get information about current company stock value")
    public Mono<StockResponseDto> getLastStockInfoBySymbol(@RequestHeader(name = API_KEY_HEADER) @Valid String apiKey,
                                                           @PathVariable
                                                           @Pattern(
                                                               regexp = "^\\w{1,5}$",
                                                               message = "Invalid stock code format") @Valid String stock_code) {
        return stockService.getLastChangedBySymbol(stock_code);
    }

    @GetMapping("/stocks/{stock_code:^\\w{1,5}$}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get information about all company stocks")
    public Flux<StockResponseDto> getAllStocksBySymbol(@RequestHeader(name = API_KEY_HEADER) @Valid String apiKey,
                                                       @PathVariable String stock_code) {
        return stockService.getAllBySymbol(stock_code);
    }
}