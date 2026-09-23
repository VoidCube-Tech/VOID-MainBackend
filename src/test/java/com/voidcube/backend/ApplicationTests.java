package com.voidcube.backend;

import com.voidcube.backend.v1.company.api.CompanyController;
import com.voidcube.backend.v1.events.api.EventController;
import com.voidcube.backend.v1.identity.api.AuthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ApplicationTests {

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
