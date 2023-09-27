package com.proselyteapi.dataprovider.cron;

import com.proselyteapi.dataprovider.dto.CompanyRequestDto;
import com.proselyteapi.dataprovider.entity.Company;
import com.proselyteapi.dataprovider.generator.DataGeneratingService;
import com.proselyteapi.dataprovider.mapper.CompanyMapper;
import com.proselyteapi.dataprovider.repository.CompanyRepository;
import com.proselyteapi.dataprovider.repository.StockRepository;
import com.proselyteapi.dataprovider.service.BaseServiceTest;
import com.proselyteapi.dataprovider.service.CompanyService;
import com.proselyteapi.dataprovider.service.StockService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testcontainers.shaded.org.awaitility.Durations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(properties = "scheduling.enabled=true")
public class CronJobTest {

    @Mock
    private DataGeneratingService dataGeneratingService;
    @Mock
    private CompanyService companyService;
    @Mock
    private StockService stockService;
    @SpyBean
    private CronJob cronJob;

    private CompanyMapper companyMapper = CompanyMapper.MAPPER;

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