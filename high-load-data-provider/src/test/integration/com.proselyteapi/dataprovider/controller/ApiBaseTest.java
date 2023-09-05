package com.proselyteapi.dataprovider.controller;

import annotation.Integration;
import com.proselyteapi.dataprovider.HighLoadDataProviderApplication;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;


@AutoConfigureWebTestClient
@Integration
//@SpringJUnitWebConfig
//@WebFluxTest
@SpringBootTest(
        classes = {
    HighLoadDataProviderApplication.class,
    TestConfiguration.class
},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:/application-test.yaml")
public abstract class ApiBaseTest {

    protected WebTestClient webTestClient;

    @Autowired
    private ApplicationContext context;

    protected HttpHeaders commonHeaders;

    @BeforeEach
    void setUp() {
        webTestClient =
                WebTestClient.bindToApplicationContext(context)
                        .build();
//                WebTestClient
//                .bindToServer()
//                .baseUrl("http://localhost:8083")
//                .build();


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

    protected void removeHeader(String header) {
        commonHeaders.remove(header);
    }
}