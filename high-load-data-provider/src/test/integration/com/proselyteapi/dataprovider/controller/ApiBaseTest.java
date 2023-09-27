package com.proselyteapi.dataprovider.controller;

import annotation.Integration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proselyteapi.dataprovider.HighLoadDataProviderApplication;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.MockServerConfigurer;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;


@AutoConfigureWebTestClient(timeout = "10000000")
@Integration
//@SpringJUnitWebConfig
//@WebFluxTest
@SpringBootTest(
    classes = {
        HighLoadDataProviderApplication.class,
        TestConfiguration.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:/application-test.yaml")
public abstract class ApiBaseTest {

//    private MockWebServer server;

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected ObjectMapper objectMapper;

    protected HttpHeaders commonHeaders;

    @Container
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

    @BeforeEach
    void setUp() {
        container.setWaitStrategy(
            new LogMessageWaitStrategy()
                .withRegEx(".*database system is ready to accept connections.*\\s")
                .withTimes(1)
                .withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS))
        );
        container.start();
//        server = new MockWebServer();
//        webTestClient =
//            WebTestClient
//                .bindToServer()
////                .baseUrl("http://localhost:" + serverProperties.getPort())
//                .build();

        this.setAuthorizationHeader();
    }

    @AfterEach
    public void after(){
//        server.shutdown();
    }

    public void setAuthorizationHeader() {
        commonHeaders = new HttpHeaders();
        commonHeaders.set("x-client-id", "test-service");
        commonHeaders.set("x-client-version", "v1.0.0");
        commonHeaders.set("Authorization", "Bearer xxxx");
    }

    public void removeAuthorizationHeader() {
        commonHeaders = new HttpHeaders();
        commonHeaders.set("x-client-id", "test-service");
        commonHeaders.set("x-client-version", "v1.0.0");
        commonHeaders.set("Authorization", "");
    }
}