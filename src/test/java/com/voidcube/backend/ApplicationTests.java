package com.voidcube.backend;

import com.voidcube.backend.v1.company.api.CompanyController;
import com.voidcube.backend.v1.events.api.EventController;
import com.voidcube.backend.v1.events.infrastructure.persistence.repository.LegacyUserReferenceRepository;
import com.voidcube.backend.v1.events.infrastructure.persistence.repository.ServiceUserRepository;
import com.voidcube.backend.v1.identity.api.AuthController;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ApplicationTests {

    @TestConfiguration
    static class TestMockConfig {
        @Bean
        public ServiceUserRepository serviceUserRepository() {
            return Mockito.mock(ServiceUserRepository.class);
        }

        @Bean
        public LegacyUserReferenceRepository legacyUserReferenceRepository() {
            return Mockito.mock(LegacyUserReferenceRepository.class);
        }

        @Bean
        public com.voidcube.backend.v1.events.infrastructure.persistence.repository.EventRepository eventRepository() {
            return Mockito.mock(com.voidcube.backend.v1.events.infrastructure.persistence.repository.EventRepository.class);
        }

        @Bean
        public com.voidcube.backend.v1.events.infrastructure.persistence.repository.EventSessionRepository eventSessionRepository() {
            return Mockito.mock(com.voidcube.backend.v1.events.infrastructure.persistence.repository.EventSessionRepository.class);
        }
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
        assertNotNull(applicationContext.getBean(EventController.class));
        assertNotNull(applicationContext.getBean(CompanyController.class));
        assertNotNull(applicationContext.getBean(AuthController.class));
    }
}