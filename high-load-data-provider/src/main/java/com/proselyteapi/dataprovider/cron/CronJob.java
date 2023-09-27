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
    @Scheduled(initialDelay = 5000, fixedDelay = Long.MAX_VALUE, timeUnit = TimeUnit.NANOSECONDS)
    public void onStartupGenerateSymbolsAndNamesJob() {

        companyService.getAllCompaniesWithoutStocks()
            .count()
            .doOnNext(count -> {
                if (count.equals(0L)) {
                    CompletableFuture.allOf(CompletableFuture.runAsync(dataGeneratingService::fillSymbolsList),
                            CompletableFuture.runAsync(dataGeneratingService::fillCompanyNamesQueue))
                        .join();

                    CompletableFuture
                        .supplyAsync(dataGeneratingService::createCompanies)
                        .thenApply(result -> companyService.saveCompanies(result)
                            .doOnNext(r -> dataGeneratingService.fillCompanyIdsMap(r.getSymbol(), r.getId())).subscribe())
                        .join();
                } else {
                    companyService.getAllCompaniesWithoutStocks()
                        .doOnNext(result ->
                            CompletableFuture.allOf(CompletableFuture.runAsync(() -> dataGeneratingService.addSymbolToList(result.getSymbol())),
                                    CompletableFuture.runAsync(() -> dataGeneratingService.addIdToList(result.getId(), result.getSymbol())))
                                .join())
                        .subscribe();
                }
            })
            .subscribe();
    }

    @Async("asyncStockExecutor")
    @Scheduled(cron = "*/30 * * * * *")
    public void generateStockDataJob() {
        CompletableFuture.supplyAsync(dataGeneratingService::createStocks)
            .thenAccept(result -> stockService.createStocks(result)
                .subscribe())
            .join();
    }
}