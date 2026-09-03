package com.example.demo.global.datasource.config;

import com.example.demo.global.datasource.factory.DataSourceFactory;
import com.example.demo.global.datasource.shard.config.ShardingDataSourceProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "datasource")
public class DataSourceConfig {

    private ShardingDataSourceProperty history;

    @Bean(defaultCandidate = false)
    @Qualifier("historyDataSource")
    public DataSource historyDataSource(
            DataSourceFactory dataSourceFactory
    ) {
        return dataSourceFactory.createDataSource(history);
    }

}
