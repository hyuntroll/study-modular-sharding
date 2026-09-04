package com.example.demo.global.datasource.application.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan("com.example.demo.adapter.out.persistence.application")
@EnableJpaRepositories(
        basePackages =
                "com.example.demo.adapter.out.persistence.application",
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
public class ApplicationJpaConfig {
}
