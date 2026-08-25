package com.example.demo.global.datasource.config;

import com.example.demo.global.datasource.shard.config.ShardingConfig;
import com.example.demo.global.datasource.shard.config.ShardingDataSourceProperty;
import com.example.demo.global.datasource.shard.config.ShardingProperty;
import com.example.demo.global.datasource.shard.enums.ShardingTarget;
import com.example.demo.global.datasource.shard.router.DataSourceRouter;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Bean(defaultCandidate = false)
    @Qualifier("historyDataSource")
    public DataSource historyDataSource() {
        return switch(history.getMode()) {
            case SINGLE -> createSingleDataSource(history);
            case SHARDED -> createShardingDataSource(history, ShardingConfig.getShardingPropertyMap().get(ShardingTarget.HISTORY));
        };
    }

    private DataSource createSingleDataSource(
            ShardingDataSourceProperty property
    ) {
        return dataSource(
                property.getSingle().getUsername(),
                property.getSingle().getPassword(),
                property.getSingle().getUrl()
        );
    }

    private DataSource createShardingDataSource(
            ShardingDataSourceProperty property,
            ShardingProperty shardingProperty
    ) {
        DataSourceRouter router = new DataSourceRouter(shardingProperty);
        Map<Object, Object> dataSourceMap = new LinkedHashMap<>();

        for (int i =0; i < property.getShards().size(); i++) {
            ShardingDataSourceProperty.Shard shard  = property.getShards().get(i);

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
