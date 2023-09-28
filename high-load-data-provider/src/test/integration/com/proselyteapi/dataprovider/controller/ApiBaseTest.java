package com.proselyteapi.dataprovider.controller;

import annotation.Integration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proselyteapi.dataprovider.HighLoadDataProviderApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;


@AutoConfigureWebTestClient(timeout = "10000000")
@Integration
@SpringBootTest(
    classes = {
        HighLoadDataProviderApplication.class,
        TestConfiguration.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "scheduling.enabled=false")
@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:/application-test.yaml")
public abstract class ApiBaseTest {

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected ObjectMapper objectMapper;

    protected HttpHeaders commonHeaders;

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
    static void beforeAll() {
        container.setWaitStrategy(
            new LogMessageWaitStrategy()
                .withRegEx(".*database system is ready to accept connections.*\\s")
                .withTimes(1)
                .withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS))
        );
        container.start();
    }

    @AfterAll
    static void afterAll() {
    }

    public void setAuthorizationHeader() {
        commonHeaders = new HttpHeaders();
        commonHeaders.set("x-client-id", "test-service");
        commonHeaders.set("x-client-version", "v1.0.0");
        commonHeaders.set("Authorization", "Bearer xxxx");
    }
}