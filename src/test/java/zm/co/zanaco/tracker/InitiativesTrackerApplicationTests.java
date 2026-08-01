package zm.co.zanaco.tracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Smoke test: verifies the Spring application context loads without errors.
 *
 * Overrides the datasource with an in-memory H2 database so the test is
 * self-contained and does not require a live PostgreSQL instance.
 * Flyway is disabled because H2 cannot parse PostgreSQL-specific DDL;
 * ddl-auto=create-drop lets Hibernate derive the schema from the entities.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.flyway.enabled=false",
        "jwt.secret=test-only-secret-at-least-32-characters-long"
})
class InitiativesTrackerApplicationTests {

    @Test
    void contextLoads() {
    }
}
