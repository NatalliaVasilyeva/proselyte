package integration.com.proselyteapi.dataprovider.controller;

import annotation.Integration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Integration
class AuthControllerTest extends ApiBaseTest {

    @Test
    void fakeTest() {
        assertTrue(true);
    }
}