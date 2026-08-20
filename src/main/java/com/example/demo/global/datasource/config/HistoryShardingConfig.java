package com.example.demo.global.datasource.config;

import com.example.demo.global.datasource.enums.ShardingTarget;
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
