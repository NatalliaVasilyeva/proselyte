package integration.com.proselyteapi.dataprovider.controller;

import annotation.Integration;
import com.proselyteapi.dataprovider.HighLoadDataProviderApplication;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Integration
@SpringJUnitWebConfig
@SpringBootTest(classes = {
    HighLoadDataProviderApplication.class,
    TestConfiguration.class
}, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:/application-test.yaml")
@AutoConfigureObservability
public abstract class ApiBaseTest {

    protected MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    protected HttpHeaders commonHeaders;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .alwaysDo(MockMvcResultHandlers.print())
            .build();

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