package com.example.demo.global.datasource.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = {
                "com.example.demo.adapter.out.persistence.application"
        },
        entityManagerFactoryRef = "applicationEntityManagerFactory",
        transactionManagerRef = "applicationTransactionManager"
)
public class ApplicationJpaConfig {

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean applicationEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("applicationDataSource") DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
                .packages(
                        "com.example.demo.adapter.out.persistence.application"
                )
                .persistenceUnit("application")
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager applicationTransactionManager(
            @Qualifier("applicationEntityManagerFactory")
            EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
