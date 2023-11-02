package com.proselyteapi.stocksreceiver.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class ConnectorConfiguration {

    @Bean
    public WebClient webClient(@Value("${service.source.host}") String sourceHost,
                               @Value("${webClient.readWriteTimeoutMs:20000}") int webClientTimeoutMs) {
        return WebClient.builder()
            .baseUrl(sourceHost)
            .clientConnector(
                new ReactorClientHttpConnector(
                    HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, webClientTimeoutMs)
                        .doOnConnected(connection -> {
                            connection.addHandlerLast(new ReadTimeoutHandler(webClientTimeoutMs, TimeUnit.MILLISECONDS));
                            connection.addHandlerLast(new WriteTimeoutHandler(webClientTimeoutMs, TimeUnit.MILLISECONDS));
                        })
                )
            ).build();

    }

}