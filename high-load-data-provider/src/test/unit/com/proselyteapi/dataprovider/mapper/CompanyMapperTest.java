package com.proselyteapi.dataprovider.mapper;

import annotation.Unit;
import com.proselyteapi.dataprovider.dto.CompanyRequestDto;
import com.proselyteapi.dataprovider.entity.Company;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Unit
class CompanyMapperTest {

    @Test
    void shouldReturnCorrectEntity() {
        var companyDto = CompanyRequestDto.builder()
            .name("aaaa")
            .symbol("aaaaa")
            .enabled(false)
            .build();

        var company = CompanyMapper.MAPPER.map(companyDto);

        assertEquals(companyDto.getName(), company.getName());
        assertEquals(companyDto.getSymbol(), company.getSymbol());
        assertEquals(companyDto.isEnabled(), company.isEnabled());
        assertThat(company.getCreatedAt()).isNull();
        assertThat(company.getUpdatedAt()).isNull();
        assertThat(company.getId()).isNull();
    }

    @Test
    void shouldReturnCorrectEntities() {
        var companyDtoFirst = CompanyRequestDto.builder()
            .name("aaaa")
            .symbol("aaaaa")
            .enabled(false)
            .build();

        var companyDtoSecond = CompanyRequestDto.builder()
            .name("bbbb")
            .symbol("bbbb")
            .enabled(false)
            .build();

        var companies = CompanyMapper.MAPPER.mapCompanyDtoList(List.of(companyDtoFirst, companyDtoSecond));

        assertThat(companies).hasSize(2);
        var companyOne = companies.get(0);
        assertEquals(companyDtoFirst.getName(), companyOne.getName());
        assertEquals(companyDtoFirst.getSymbol(), companyOne.getSymbol());
        assertEquals(companyDtoFirst.isEnabled(), companyOne.isEnabled());
        assertThat(companyOne.getCreatedAt()).isNull();
        assertThat(companyOne.getUpdatedAt()).isNull();
        assertThat(companyOne.getId()).isNull();

        var companyTwo = companies.get(1);
        assertEquals(companyDtoSecond.getName(), companyTwo.getName());
        assertEquals(companyDtoSecond.getSymbol(), companyTwo.getSymbol());
        assertEquals(companyDtoSecond.isEnabled(), companyTwo.isEnabled());
        assertThat(companyTwo.getCreatedAt()).isNull();
        assertThat(companyTwo.getUpdatedAt()).isNull();
        assertThat(companyTwo.getId()).isNull();
    }

    @Test
    void shouldReturnCorrectDto() {
        var company = Company.builder()
            .id(1L)
            .name("aaaa")
            .enabled(true)
            .symbol("aaaaa")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        var companyResponseDto = CompanyMapper.MAPPER.map(company);

        assertEquals(company.getId(), companyResponseDto.getId());
        assertEquals(company.getName(), companyResponseDto.getName());
        assertEquals(company.getSymbol(), companyResponseDto.getSymbol());
        assertTrue(companyResponseDto.isEnabled());
    }

    @Test
    void shouldReturnCorrectDtos() {
        var companyOne = Company.builder()
            .id(1L)
            .name("aaaa")
            .enabled(true)
            .symbol("aaaaa")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        var companyTwo = Company.builder()
            .id(2L)
            .name("bbbb")
            .enabled(true)
            .symbol("bbbbb")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        var companiesResponseDtos = CompanyMapper.MAPPER.mapCompanyList(List.of(companyOne, companyTwo));

        assertThat(companiesResponseDtos).hasSize(2);
        var companyResponseDtoOne = companiesResponseDtos.get(0);
        assertEquals(companyOne.getId(), companyResponseDtoOne.getId());
        assertEquals(companyOne.getName(), companyResponseDtoOne.getName());
        assertEquals(companyOne.getSymbol(), companyResponseDtoOne.getSymbol());
        assertEquals(companyOne.isEnabled(), companyResponseDtoOne.isEnabled());

        var companyResponseDtoTwo = companiesResponseDtos.get(1);
        assertEquals(companyTwo.getId(), companyResponseDtoTwo.getId());
        assertEquals(companyTwo.getName(), companyResponseDtoTwo.getName());
        assertEquals(companyTwo.getSymbol(), companyResponseDtoTwo.getSymbol());
        assertEquals(companyTwo.isEnabled(), companyResponseDtoTwo.isEnabled());
    }
}