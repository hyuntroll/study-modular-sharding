package com.example.demo.global.datasource.config;

import com.example.demo.global.datasource.router.DataSourceRouter;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "datasource")
public class DataSourceConfig {

    public static final String SHARD_DELIMITER = "_";

    private ShardingDataSourceProperty history;

    private DataSourceProperty application;

    @Primary
    @Bean
    public DataSource applicationDataSource() {
        HikariDataSource dataSource = new HikariDataSource();

        dataSource.setUsername(application.getUsername());
        dataSource.setPassword(application.getPassword());
        dataSource.setJdbcUrl(application.getUrl());

        return dataSource;
    }

    @Bean
    public DataSource historyDataSource() {
        DataSourceRouter router = new DataSourceRouter();
        Map<Object, Object> dataSourceMap = new LinkedHashMap<>();

        for (int i =0; i < history.getShards().size(); i++) {
            ShardingDataSourceProperty.Shard shard  = history.getShards().get(i);

            DataSource masterDs = dataSource(
                    shard.getUsername(),
                    shard.getPassword(),
                    shard.getMaster().getUrl()
            );
            dataSourceMap.put(i + SHARD_DELIMITER + shard.getMaster().getName(), masterDs);
        }

        router.setTargetDataSources(dataSourceMap);
        router.afterPropertiesSet();

        return new LazyConnectionDataSourceProxy(router);
    }

    private DataSource dataSource(
            String username,
            String password,
            String url
    ) {
        HikariDataSource dataSource = new HikariDataSource();

        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setJdbcUrl(url);

        return dataSource;
    }
}
