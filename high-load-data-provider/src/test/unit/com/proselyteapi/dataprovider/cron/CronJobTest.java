package com.proselyteapi.dataprovider.cron;

import com.proselyteapi.dataprovider.generator.DataGeneratingService;
import com.proselyteapi.dataprovider.mapper.CompanyMapper;
import com.proselyteapi.dataprovider.service.CompanyService;
import com.proselyteapi.dataprovider.service.StockService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testcontainers.shaded.org.awaitility.Durations;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(properties = "scheduling.enabled=true")
class CronJobTest {

    @SpyBean
    private CronJob cronJob;

    @Test
    void onStartupGenerateSymbolsAndNamesJobScheduledTest() {
        await()
            .pollDelay(5000, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> verify(cronJob, atMostOnce()).onStartupGenerateSymbolsAndNamesJob());
    }

    @Test
    void generateStockDataJobScheduledTest() {
        await()
            .atMost(Durations.TEN_SECONDS)
            .untilAsserted(() -> verify(cronJob, atMost(1)).generateStockDataJob());
    }
}