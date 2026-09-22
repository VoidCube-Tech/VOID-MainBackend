package com.voidcube.backend

import com.voidcube.backend.v1.company.api.CompanyController
import com.voidcube.backend.v1.events.api.EventController
import com.voidcube.backend.v1.identity.api.AuthController
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext

@SpringBootTest
class ApplicationTests {

	@Autowired
	private lateinit var applicationContext: ApplicationContext

	@Test
	fun contextLoads() {
		assertNotNull(applicationContext)
		assertNotNull(applicationContext.getBean(EventController::class.java))
		assertNotNull(applicationContext.getBean(CompanyController::class.java))
		assertNotNull(applicationContext.getBean(AuthController::class.java))
	}

}
