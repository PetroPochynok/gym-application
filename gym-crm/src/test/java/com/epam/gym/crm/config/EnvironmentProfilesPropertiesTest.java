package com.epam.gym.crm.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class EnvironmentProfilesPropertiesTest {

    @Test
    void shouldUseLocalAsDefaultProfile() throws IOException {
        Properties properties = load("application.properties");

        assertEquals("local", properties.getProperty("spring.profiles.default"));
    }

    @Test
    void shouldContainSharedApplicationSettingsInBaseConfig() throws IOException {
        Properties properties = load("application.properties");

        assertEquals("true", properties.getProperty("spring.jpa.format-sql"));
        assertEquals("org.hibernate.dialect.MySQL8Dialect", properties.getProperty("spring.jpa.properties.hibernate.dialect"));
        assertEquals("ant_path_matcher", properties.getProperty("spring.mvc.pathmatch.matching-strategy"));
        assertEquals("/gym-crm", properties.getProperty("server.servlet.context-path"));
        assertEquals("health,info,metrics,prometheus", properties.getProperty("management.endpoints.web.exposure.include"));
    }

    @Test
    void shouldContainDedicatedDatabaseAndJpaFlagsPerEnvironment() throws IOException {
        Properties local = load("application-local.properties");
        Properties dev = load("application-dev.properties");
        Properties stg = load("application-stg.properties");
        Properties prod = load("application-prod.properties");

        assertEquals("jdbc:mysql://localhost:3306/gym_crm_local", local.getProperty("spring.datasource.url"));
        assertEquals("update", local.getProperty("spring.jpa.hibernate.ddl-auto"));
        assertEquals("true", local.getProperty("spring.jpa.show-sql"));

        assertEquals("jdbc:mysql://dev-db:3306/gym_crm_dev", dev.getProperty("spring.datasource.url"));
        assertEquals("validate", dev.getProperty("spring.jpa.hibernate.ddl-auto"));
        assertEquals("false", dev.getProperty("spring.jpa.show-sql"));

        assertEquals("jdbc:mysql://stg-db:3306/gym_crm_stg", stg.getProperty("spring.datasource.url"));
        assertEquals("validate", stg.getProperty("spring.jpa.hibernate.ddl-auto"));
        assertEquals("false", stg.getProperty("spring.jpa.show-sql"));

        assertEquals("jdbc:mysql://prod-db:3306/gym_crm_prod", prod.getProperty("spring.datasource.url"));
        assertEquals("validate", prod.getProperty("spring.jpa.hibernate.ddl-auto"));
        assertEquals("false", prod.getProperty("spring.jpa.show-sql"));

        assertNotEquals(local.getProperty("spring.datasource.url"), dev.getProperty("spring.datasource.url"));
        assertNotEquals(local.getProperty("spring.datasource.url"), prod.getProperty("spring.datasource.url"));
    }

    private Properties load(String fileName) throws IOException {
        Properties properties = new Properties();
        properties.load(new ClassPathResource(fileName).getInputStream());
        return properties;
    }

}
