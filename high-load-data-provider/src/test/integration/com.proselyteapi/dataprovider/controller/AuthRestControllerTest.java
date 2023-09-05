package com.proselyteapi.dataprovider.controller;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthRestControllerTest extends ApiBaseTest {

    @Test
    void fakeTest() {
        assertTrue(true);
    }

    @Test
    void contextLoads(ApplicationContext context) {
        assertThat(context).isNotNull();
    }
}