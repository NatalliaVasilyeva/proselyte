package com.proselyteapi.stocksreceiver.connector;

import com.proselyteapi.stocksreceiver.dto.CompanyDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.lang.invoke.MethodHandles;

@Slf4j
@AllArgsConstructor
@Component
public class CompanyConnector {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final ParameterizedTypeReference<String> STRING_PARAMETERIZED_TYPE_REFERENCE = new ParameterizedTypeReference<>() {
    };

    private WebClient webClient;

    @Value("${service.source.token}")
    private String token;


    public Flux<CompanyDto> getCompanies() {
        return requestCompanies("ref_data");
    }

    private Flux<CompanyDto> requestCompanies(String pathSegment) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder.pathSegment(pathSegment)
                .queryParam("token", token)
                .build())
            .exchangeToFlux(response -> {
                if (response.statusCode().isError() || response.statusCode().is3xxRedirection() || response.statusCode().is1xxInformational()) {
                    response.bodyToMono(STRING_PARAMETERIZED_TYPE_REFERENCE).doOnNext(message -> LOG.error("Wrong status code " + message));
                }
                return response.bodyToFlux(CompanyDto.class);
            })
            .doOnError(throwable -> LOG.error("Exception occurred while making request", throwable));
    }

}