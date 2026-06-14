package com.epam.gym.crm.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;

@Component
public class JpaEntitiesHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(JpaEntitiesHealthIndicator.class);
    private final EntityManagerFactory entityManagerFactory;

    public JpaEntitiesHealthIndicator(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public Health health() {
        try {
            int entitiesCount = entityManagerFactory.getMetamodel().getEntities().size();

            if (entitiesCount == 0) {
                return Health.down()
                        .withDetail("entitiesCount", 0)
                        .withDetail("reason", "No JPA entities registered")
                        .build();
            }

            return Health.up()
                    .withDetail("entitiesCount", entitiesCount)
                    .build();
        } catch (Exception e) {
            log.error("JPA Entities health check failed", e);
            return Health.down(e)
                    .withDetail("reason", "Failed to inspect JPA metamodel")
                    .build();
        }
    }
}