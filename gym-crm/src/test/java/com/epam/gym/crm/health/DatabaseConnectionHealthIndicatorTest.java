package com.epam.gym.crm.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseConnectionHealthIndicatorTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData metaData;

    private DatabaseConnectionHealthIndicator healthIndicator;

    private void initIndicatorWithDefaultProduct() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("MySQL");
        healthIndicator = new DatabaseConnectionHealthIndicator(dataSource);
    }

    @Test
    void shouldReturnUpWhenConnectionIsValid() throws SQLException {
        initIndicatorWithDefaultProduct();
        when(connection.isValid(2)).thenReturn(true);

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("Connection is valid", health.getDetails().get("customDbHealth"));
        assertEquals("MySQL", health.getDetails().get("databaseProduct"));
    }

    @Test
    void shouldReturnDownWhenConnectionIsNotValid() throws SQLException {
        initIndicatorWithDefaultProduct();
        when(connection.isValid(2)).thenReturn(false);

        Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("Connection is not valid", health.getDetails().get("customDbHealth"));
    }

    @Test
    void shouldReturnDownWhenExceptionOccurs() throws SQLException {
        initIndicatorWithDefaultProduct();
        reset(dataSource);
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection timeout"));

        Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("Failed to validate DB connection", health.getDetails().get("customDbHealth"));
    }

    @Test
    void shouldFallbackToUnknownProductWhenInitFails() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Init error"));
        healthIndicator = new DatabaseConnectionHealthIndicator(dataSource);

        reset(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("UNKNOWN", health.getDetails().get("databaseProduct"));
    }
}