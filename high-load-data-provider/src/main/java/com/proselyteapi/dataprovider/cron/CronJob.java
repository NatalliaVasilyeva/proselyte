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
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class CronJob {

    private final DataGeneratingService dataGeneratingService;
    private final CompanyService companyService;
    private final StockService stockService;


    @Async("asyncCompanyExecutor")
    @Scheduled(initialDelay = 100, fixedDelay = Long.MAX_VALUE, timeUnit = TimeUnit.NANOSECONDS)
    public void onStartupGenerateSymbolsAndNamesJob() {
        CompletableFuture.runAsync(dataGeneratingService::fillSymbolsList)
                .thenRun(dataGeneratingService::fillCompanyNamesQueue)
                .join();

        CompletableFuture
                .supplyAsync(dataGeneratingService::createCompanies)
                .thenApply(companyService::createCompanies)
                .thenAccept(companyResponseDtoFlux -> {

                    System.out.println("after saving");
                    companyResponseDtoFlux
                        .collectList()
                            .doOnNext(companyResponseDtos -> {
                            System.out.println("HERE " + companyResponseDtos.toString());
                            companyResponseDtos.forEach(companyResponseDto -> dataGeneratingService.fillCompanyIdsMap(companyResponseDto.getSymbol(), companyResponseDto.getId()));
                        });
                })
                .join();
    }

    @Async("asyncStockExecutor")
    @Scheduled(cron = "*/150 * * * * *")
    public void generateStockDataJob() {
        System.out.println("start");
        CompletableFuture.supplyAsync(dataGeneratingService::createStocks)
                .thenAccept(stockService::createStocks)
                .join();
        System.out.println("finish");
    }
}
