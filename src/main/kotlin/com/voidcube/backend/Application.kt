package com.voidcube.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.voidcube.backend"])
class Application

fun main(args: Array<String>) {
	runApplication<Application>(*args)
}
