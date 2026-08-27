package com.example.demo.global.datasource.factory;

import com.example.demo.global.datasource.shard.config.DataSourceProperty;
import com.example.demo.global.datasource.shard.config.ShardingDataSourceProperty;
import com.example.demo.global.datasource.shard.router.DataSourceRouter;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

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
        return dataSource(
                property.getUsername(),
                property.getPassword(),
                property.getUrl()
        );
    }

    private DataSource createShardDataSource(
            ShardingDataSourceProperty property
    ) {
        DataSourceRouter router = new DataSourceRouter(property.getShard());
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
