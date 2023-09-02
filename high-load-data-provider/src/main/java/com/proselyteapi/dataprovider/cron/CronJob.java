package com.proselyteapi.dataprovider.cron;


import com.proselyteapi.dataprovider.generator.DataGeneratingService;
import com.proselyteapi.dataprovider.service.CompanyService;
import com.proselyteapi.dataprovider.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class CronJob {

    private final DataGeneratingService dataGeneratingService;
    private final CompanyService companyService;
    private final StockService stockService;


    @Async
    @Scheduled(cron = "@reboot")
    public void onStartupGenerateSymbolAndCompanyDataJob() {
        CompletableFuture.runAsync(dataGeneratingService::fillSymbolsList)
                .thenRunAsync(dataGeneratingService::fillCompanyNamesQueue);
        CompletableFuture
                .supplyAsync(dataGeneratingService::createCompanies)
                .thenApplyAsync(companyService::createCompanies)
                .thenAccept(companyResponseDtoFlux ->
                        companyResponseDtoFlux
                                .collectList()
                                .flatMapMany(companyResponseDtos -> {
                                    companyResponseDtos.forEach(companyResponseDto -> dataGeneratingService.fillCompanyIdsMap(companyResponseDto.getSymbol(), companyResponseDto.getId()));
                                    return companyResponseDtoFlux;
                                }))
                .join();
    }

    @Async
    @Scheduled(cron = "*/10 * * * * *")
    public void generateStockDataJob() {
        CompletableFuture.supplyAsync(dataGeneratingService::createStock)
                .thenAccept(stockService::createStock)
                .join();
    }
}
