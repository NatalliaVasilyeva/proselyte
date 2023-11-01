package com.proselyteapi.dataprovider.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.proselyteapi.dataprovider.dto.CompanyRequestDto;
import com.proselyteapi.dataprovider.dto.CompanyResponseDto;
import com.proselyteapi.dataprovider.entity.Company;
import com.proselyteapi.dataprovider.repository.CompanyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CompanyRestControllerIT extends ApiBaseTest {

    @Autowired
    private CompanyRepository companyRepository;
    private Company existingCompany;

    @BeforeEach
    public void setup() {
       existingCompany = companyRepository.save(
            Company.builder()
                .name("test_company")
                .enabled(true)
                .symbol("test")
                .build())
            .block();
    }

    @AfterEach
    public void teardown() {
        companyRepository.deleteAll().subscribe();
    }

    @Test
    @WithMockUser(username = "user", authorities = {"USER", "ADMIN"})
    void getAllCompaniesTest() {

        webTestClient.get().uri("/api/v1/companies")
            .accept(MediaType.APPLICATION_JSON)
            .header("x-api-key", "aaa")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(CompanyResponseDto.class)
            .consumeWith(response -> {
                List<CompanyResponseDto> companies = response.getResponseBody();
                assertThat(companies).isNotEmpty();
            });
    }

    @Test
    @WithMockUser(username = "user", authorities = {"USER", "ADMIN"})
    void createCompanyTest() throws JsonProcessingException {

        var companyRequestDto = CompanyRequestDto.builder()
            .name("test_create_company")
            .enabled(true)
            .symbol("TCC")
            .build();

        final String jsonBody = objectMapper.writeValueAsString(companyRequestDto);


        webTestClient.post().uri("/api/v1/company")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header("x-api-key", "aaa")
            .bodyValue(jsonBody)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(CompanyResponseDto.class)
            .consumeWith(response -> {
                var bodyCompany = response.getResponseBody();
                assertEquals(companyRequestDto.getName(), bodyCompany.getName());
                assertEquals(companyRequestDto.getSymbol(), bodyCompany.getSymbol());
                assertEquals(companyRequestDto.isEnabled(), bodyCompany.isEnabled());

            });
    }

    @Test
    @WithMockUser(username = "user", authorities = {"USER", "ADMIN"})
    void createCompaniesTest() throws JsonProcessingException {

        var companyRequestDtoOne = CompanyRequestDto.builder()
            .name("test_create_company_one")
            .enabled(true)
            .symbol("TCCO")
            .build();

        var companyRequestDtoTwo = CompanyRequestDto.builder()
            .name("test_create_company_two")
            .enabled(true)
            .symbol("TCCT")
            .build();

        final String jsonBody = objectMapper.writeValueAsString(List.of(companyRequestDtoOne, companyRequestDtoTwo));


        webTestClient.post().uri("/api/v1/companies")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header("x-api-key", "aaa")
            .bodyValue(jsonBody)
            .exchange()
            .expectStatus().isCreated()
            .expectBodyList(CompanyResponseDto.class)
            .hasSize(2)
            .consumeWith(response -> {
                List<CompanyResponseDto> companies = response.getResponseBody();
                var companyOne = companies.get(0);
                assertEquals(companyRequestDtoOne.getName(), companyOne.getName());
                assertEquals(companyRequestDtoOne.getSymbol(), companyOne.getSymbol());
                assertEquals(companyRequestDtoOne.isEnabled(), companyOne.isEnabled());

                var companyTwo = companies.get(1);
                assertEquals(companyRequestDtoTwo.getName(), companyTwo.getName());
                assertEquals(companyRequestDtoTwo.getSymbol(), companyTwo.getSymbol());
                assertEquals(companyRequestDtoTwo.isEnabled(), companyTwo.isEnabled());

            });
    }

    @Test
    @WithMockUser(username = "user", authorities = {"USER", "ADMIN"})
    void getCompanyBySymbolTest() throws JsonProcessingException {

        webTestClient.get().uri("/api/v1/companies/" + existingCompany.getSymbol())
            .accept(MediaType.APPLICATION_JSON)
            .header("x-api-key", "aaa")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(CompanyResponseDto.class)
            .hasSize(1)
            .consumeWith(response -> {
                List<CompanyResponseDto> companies = response.getResponseBody();
                var company = companies.get(0);
                assertEquals(existingCompany.getName(), company.getName());
                assertEquals(existingCompany.getSymbol(), company.getSymbol());
                assertEquals(existingCompany.isEnabled(), company.isEnabled());

            });
    }
}