package kz.bitlab.mainservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Full context test: needs a running PostgreSQL (docker compose up -d postgres).
 * Skipped by default so that `mvn test` passes without a database;
 * enable with INTEGRATION_TESTS=true.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
class MainServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
