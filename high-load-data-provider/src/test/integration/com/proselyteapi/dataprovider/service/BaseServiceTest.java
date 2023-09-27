package com.proselyteapi.dataprovider.service;

import annotation.Integration;
import com.proselyteapi.dataprovider.HighLoadDataProviderApplication;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@Integration
@SpringBootTest(classes = {
    HighLoadDataProviderApplication.class,
    TestConfiguration.class
}, webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = "scheduling.enabled=false")
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:/application-test.yaml")
@ExtendWith(MockitoExtension.class)
public abstract class BaseServiceTest {
}