package com.proselyteapi.stocksreceiver.mapper;

import annotation.Unit;
import com.proselyteapi.stocksreceiver.dto.CompanyDto;
import com.proselyteapi.stocksreceiver.entity.Company;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Unit
class CompanyMapperTest {

    @Test
    void shouldReturnCorrectEntity() {
        var companyDto = new CompanyDto("aaa", "test_name", true);

        var company = CompanyMapper.MAPPER.map(companyDto);

        assertEquals(companyDto.name(), company.getName());
        assertEquals(companyDto.symbol(), company.getSymbol());
        assertEquals(companyDto.isEnabled(), company.isEnabled());
        assertThat(company.getCreatedAt()).isNull();
        assertThat(company.getUpdatedAt()).isNull();
        assertThat(company.getId()).isNull();
    }

    @Test
    void shouldReturnCorrectDto() {
        var company = Company.builder()
            .id(1L)
            .name("test_name")
            .enabled(true)
            .symbol("aaaaa")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        var companyDto = CompanyMapper.MAPPER.map(company);

        assertEquals(company.getName(), companyDto.name());
        assertEquals(company.getSymbol(), companyDto.symbol());
        assertTrue(companyDto.isEnabled());
    }
}