package com.proselyteapi.stocksreceiver.cron;

import com.proselyteapi.stocksreceiver.cron.CronJob;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testcontainers.shaded.org.awaitility.Durations;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(properties = "scheduling.enabled=true")
class CronJobTest {

    @SpyBean
    private CronJob cronJob;

    @Test
    void retrieveCompanyDataTest() {
        await()
            .pollDelay(5000, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> verify(cronJob, atLeast(100)).retrieveCompanyData());
    }

    @Test
    void retrieveStocksDataTest() {
        await()
            .atMost(5000, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> verify(cronJob, atMost(1)).retrieveStocksData());
    }

    @Test
    void printStocksAnalyticsTest() {
        await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> verify(cronJob, atLeast(2)).printStocksAnalytics());
    }
}