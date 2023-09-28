package com.proselyteapi.dataprovider.service;

import annotation.Integration;
import com.proselyteapi.dataprovider.HighLoadDataProviderApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Integration
@SpringBootTest(classes = {
    HighLoadDataProviderApplication.class,
    TestConfiguration.class
}, webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = "scheduling.enable=false")
@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:/application-test.yaml")
@ExtendWith(MockitoExtension.class)
public abstract class BaseServiceTest {

    private static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>()
        .withDatabaseName("data-provider-test")
        .withUsername("data-provider-test")
        .withPassword("data-provider-test")
        .withInitScript("init.sql");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.liquibase.contexts", () -> "!prod");
        registry.add("spring.liquibase.url", () -> container.getJdbcUrl());
        registry.add("spring.liquibase.user", () -> container.getUsername());
        registry.add("spring.password.password", () -> container.getPassword());

        registry.add("spring.r2dbc.url", () -> container.getJdbcUrl().replace("jdbc:", "r2dbc:"));
        registry.add("spring.r2dbc.username", () -> container.getUsername());
        registry.add("spring.r2dbc.password", () -> container.getPassword());
    }

    @BeforeAll
    static void setUp() {
        container.setWaitStrategy(
            new LogMessageWaitStrategy()
                .withRegEx(".*database system is ready to accept connections.*\\s")
                .withTimes(1)
                .withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS))
        );
        container.start();
    }

    @AfterEach
    public void after(){

    }
}