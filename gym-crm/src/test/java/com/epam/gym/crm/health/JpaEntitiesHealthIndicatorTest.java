package com.epam.gym.crm.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaEntitiesHealthIndicatorTest {

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @Mock
    private Metamodel metamodel;

    @InjectMocks
    private JpaEntitiesHealthIndicator healthIndicator;

    @Test
    void shouldReturnUpWhenEntitiesAreRegistered() {
        Set<EntityType<?>> mockEntities = new HashSet<>();
        mockEntities.add(mock(EntityType.class));
        mockEntities.add(mock(EntityType.class));

        when(entityManagerFactory.getMetamodel()).thenReturn(metamodel);
        when(metamodel.getEntities()).thenReturn(mockEntities);

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(2, health.getDetails().get("entitiesCount"));
    }

    @Test
    void shouldReturnDownWhenNoEntitiesRegistered() {
        when(entityManagerFactory.getMetamodel()).thenReturn(metamodel);
        when(metamodel.getEntities()).thenReturn(Collections.emptySet());

        Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(0, health.getDetails().get("entitiesCount"));
        assertEquals("No JPA entities registered", health.getDetails().get("reason"));
    }

    @Test
    void shouldReturnDownWhenExceptionOccurs() {
        when(entityManagerFactory.getMetamodel()).thenThrow(new RuntimeException("Metamodel error"));

        Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("Failed to inspect JPA metamodel", health.getDetails().get("reason"));
    }
}