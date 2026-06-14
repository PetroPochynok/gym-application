package com.epam.gym.crm.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseConnectionHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionHealthIndicator.class);

    private final DataSource dataSource;
    private final String databaseProductName;

    public DatabaseConnectionHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
        this.databaseProductName = initDatabaseProductName(dataSource);
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(2);
            if (valid) {
                return Health.up()
                        .withDetail("customDbHealth", "Connection is valid")
                        .withDetail("databaseProduct", databaseProductName)
                        .build();
            }
            return Health.down()
                    .withDetail("customDbHealth", "Connection is not valid")
                    .build();
        } catch (Exception e) {
            log.error("Database health check failed", e);
            return Health.down(e)
                    .withDetail("customDbHealth", "Failed to validate DB connection")
                    .build();
        }
    }

    private String initDatabaseProductName(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            log.warn("Could not get database product name during initialization", e);
            return "UNKNOWN";
        }
    }
}