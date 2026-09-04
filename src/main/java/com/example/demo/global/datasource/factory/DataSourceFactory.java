package com.example.demo.global.datasource.factory;

import com.example.demo.global.datasource.shard.config.DataSourceProperty;
import com.example.demo.global.datasource.shard.config.ShardingDataSourceProperty;
import com.example.demo.global.datasource.shard.router.DataSourceRouter;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public final class DataSourceFactory {
    private static final String HISTORY_MIGRATION_LOCATION = "classpath:db/migration/history";
    private static final String HISTORY_SCHEMA_HISTORY = "flyway_history_schema_history";

    public DataSource createDataSource(ShardingDataSourceProperty property) {
        if (property == null) {
            throw new IllegalArgumentException("datasource.history is required");
        }
        property.validate();

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

        return migratedDataSource(
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

        DataSourceRouter router = new DataSourceRouter(
                property.getShard(),
                property.getShards().size()
        );
        Map<Object, Object> dataSourceMap = new LinkedHashMap<>();

        for (int i =0; i < property.getShards().size(); i++) {
            log.atInfo()
                    .addKeyValue("shard_index", i)
                    .addKeyValue("shard_name", property.getShards().get(i).getMaster().getName())
                    .log("Creating shard data source");

            ShardingDataSourceProperty.Shard shard  = property.getShards().get(i);

            DataSource masterDs = migratedDataSource(
                    shard.getUsername(),
                    shard.getPassword(),
                    shard.getMaster().getUrl()
            );
            dataSourceMap.put(i, masterDs);
        }

        router.setTargetDataSources(dataSourceMap);
        router.setLenientFallback(false);
        router.afterPropertiesSet();

        return new LazyConnectionDataSourceProxy(router);
    }

    private DataSource migratedDataSource(
            String username,
            String password,
            String url
    ) {
        HikariDataSource dataSource = new HikariDataSource();

        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setJdbcUrl(url);

        try {
            Flyway.configure()
                    .dataSource(dataSource)
                    .locations(HISTORY_MIGRATION_LOCATION)
                    .table(HISTORY_SCHEMA_HISTORY)
                    .load()
                    .migrate();
            return dataSource;
        } catch (RuntimeException exception) {
            dataSource.close();
            throw exception;
        }
    }
}
