package integration.com.proselyteapi.dataprovider;

import annotation.Integration;
import com.proselyteapi.dataprovider.HighLoadDataProviderApplication;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@Integration
@SpringBootTest(classes = {
    HighLoadDataProviderApplication.class,
    TestConfiguration.class
}, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:/application-test.yaml")
@ExtendWith(MockitoExtension.class)
@AutoConfigureObservability
public abstract class BaseServiceTest {
}