package com.example.demo.global.datasource.shard.history.config;


import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = {
                "com.example.demo.adapter.out.persistence.history"
        },
        entityManagerFactoryRef = "historyEntityManagerFactory",
        transactionManagerRef = "historyTransactionManager"
)
public class HistoryJpaConfig {

    @Bean(defaultCandidate = false)
    @Qualifier("historyEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean historyEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("historyDataSource") DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
                .packages(
                        "com.example.demo.adapter.out.persistence.history"
                )
                .properties(Map.of(
                        "hibernate.hbm2ddl.auto", "none",
                        "hibernate.boot.allow_jdbc_metadata_access", "false",
                        "jakarta.persistence.database-product-name", "PostgreSQL",
                        "jakarta.persistence.database-major-version", "16"
                ))
                .persistenceUnit("history")
                .build();
    }

    @Bean(defaultCandidate = false)
    @Qualifier("historyTransactionManager")
    public PlatformTransactionManager historyTransactionManager(
            @Qualifier("historyEntityManagerFactory")
            EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
