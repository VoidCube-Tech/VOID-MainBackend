package com.voidcube.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.voidcube.backend")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
