package com.proselyteapi.dataprovider.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class CompanyServiceIT extends BaseServiceTest {

    @Autowired
    private CompanyService companyService;

    @AfterEach
    void teardown() {
        companyService.deleteAll();
    }

    @Test
    void shouldCreateCompany() {

    }

}