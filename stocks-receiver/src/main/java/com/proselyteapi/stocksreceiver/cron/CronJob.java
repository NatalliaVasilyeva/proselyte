package com.proselyteapi.stocksreceiver.cron;


import com.proselyteapi.stocksreceiver.service.DataCollectionService;
import com.proselyteapi.stocksreceiver.service.DataPrintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class CronJob {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final DataCollectionService dataCollectionService;
    private final DataPrintService dataPrintService;


    @Async()
    @Scheduled(fixedDelay = 5000 * 1000, initialDelay = 1000, timeUnit = TimeUnit.NANOSECONDS)
    public void retrieveCompanyData() {
        CompletableFuture.runAsync(dataCollectionService::collectCompaniesData)
            .thenAccept(logs -> LOG.info("All companies data was retrieved and saved"))
            .join();
    }

    @Async()
    @Scheduled(initialDelay = 5000, fixedDelay = 1000 * 1000, timeUnit = TimeUnit.NANOSECONDS)
    public void retrieveStocksData() {
        CompletableFuture.runAsync(dataCollectionService::collectStocksData)
            .thenAccept(logs -> LOG.info("All stocks data was retrieved and saved"))
            .join();
    }

    @Async()
    @Scheduled(cron = "*/5 * * * * *")
    public void printStocksAnalytics() {
        Stream.of(dataPrintService.printTopFiveExpensiveStocks(), dataPrintService.printTopFiveMostChangedStocks())
            .map(CompletableFuture::join);
    }
}