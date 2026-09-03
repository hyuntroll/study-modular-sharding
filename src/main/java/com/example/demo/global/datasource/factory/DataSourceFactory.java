package com.example.demo.global.datasource.factory;

import com.example.demo.global.datasource.shard.config.DataSourceProperty;
import com.example.demo.global.datasource.shard.config.ShardingDataSourceProperty;
import com.example.demo.global.datasource.shard.router.DataSourceRouter;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public final class DataSourceFactory {
    public static final String SHARD_DELIMITER = "_";

    public DataSource createDataSource(ShardingDataSourceProperty property) {
        return switch(property.getMode()) {
            case SINGLE -> createSingleDataSource(property.getSingle());
            case SHARDED -> createShardDataSource(property);
        };
    }

    private DataSource createSingleDataSource(
            DataSourceProperty property
    ) {
        log.atInfo()
                .addKeyValue("database_mode", "single")
                .log("Creating single data source");

        return dataSource(
                property.getUsername(),
                property.getPassword(),
                property.getUrl()
        );
    }

    private DataSource createShardDataSource(
            ShardingDataSourceProperty property
    ) {
        log.atInfo()
                .addKeyValue("database_mode", "sharded")
                .log("Creating shard data source");

        DataSourceRouter router = new DataSourceRouter(property.getShard());
        Map<Object, Object> dataSourceMap = new LinkedHashMap<>();

        for (int i =0; i < property.getShards().size(); i++) {
            log.atInfo()
                    .addKeyValue("shard_index", i)
                    .addKeyValue("shard_name", property.getShards().get(i).getMaster().getName())
                    .log("Creating shard data source");

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
