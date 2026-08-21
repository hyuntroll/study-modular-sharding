package com.example.demo.global.datasource.shard.history.config;

import com.example.demo.global.datasource.shard.config.ShardingConfig;
import com.example.demo.global.datasource.shard.config.ShardingProperty;
import com.example.demo.global.datasource.shard.enums.ShardingTarget;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sharding")
@Setter
public class HistoryShardingConfig {
    private ShardingProperty history;

    @PostConstruct
    public void init() {
        ShardingConfig.setShardingTarget(ShardingTarget.HISTORY, history);
    }

}
