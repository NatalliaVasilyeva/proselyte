package com.proselyteapi.dataprovider.service;

import com.proselyteapi.dataprovider.dto.CompanyRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


class CompanyServiceIT extends BaseServiceTest {

    @Autowired
    private CompanyService companyService;
    @AfterEach
    public void teardown() {
        companyService.deleteAll().subscribe();
    }

    @Test
    void shouldSaveCompanyTest() {

        var companyRequestDto = CompanyRequestDto.builder()
            .name("save_test_company")
            .enabled(true)
            .symbol("aaaa")
            .build();

        companyService.saveCompany(companyRequestDto)
            .doOnNext(result -> {
                assertEquals(companyRequestDto.getName(), result.getName());
                assertEquals(companyRequestDto.getSymbol(), result.getSymbol());
                assertEquals(companyRequestDto.isEnabled(), result.isEnabled());
            });
    }

    @Test
    void shouldSaveCompaniesTest() {

        var companyRequestDtoOne = CompanyRequestDto.builder()
            .name("save_test_company_one")
            .enabled(true)
            .symbol("aaaa")
            .build();

        var companyRequestDtoTwo = CompanyRequestDto.builder()
            .name("save_test_company_two")
            .enabled(true)
            .symbol("bbbb")
            .build();

        var companies = companyService.saveCompanies(List.of(companyRequestDtoOne, companyRequestDtoTwo)).collectList().block();

        assertThat(companies).hasSize(2);

        var companyOne = companies.get(0);
        assertEquals(companyRequestDtoOne.getName(), companyOne.getName());
        assertEquals(companyRequestDtoOne.getSymbol(), companyOne.getSymbol());
        assertEquals(companyRequestDtoOne.isEnabled(), companyOne.isEnabled());

        var companyTwo = companies.get(1);
        assertEquals(companyRequestDtoTwo.getName(), companyTwo.getName());
        assertEquals(companyRequestDtoTwo.getSymbol(), companyTwo.getSymbol());
        assertEquals(companyRequestDtoTwo.isEnabled(), companyTwo.isEnabled());
    }
}