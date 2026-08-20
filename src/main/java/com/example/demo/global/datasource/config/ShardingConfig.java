package com.example.demo.global.datasource.config;

import com.example.demo.global.datasource.enums.ShardingTarget;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Setter
public class ShardingConfig {
    private static Map<ShardingTarget, ShardingProperty> shardingPropertyMap = new ConcurrentHashMap<>();

    public static Map<ShardingTarget, ShardingProperty> getShardingPropertyMap() {
        return shardingPropertyMap;
    }

    public static void setShardingTarget(ShardingTarget target, ShardingProperty property) {
        shardingPropertyMap.put(target, property);
    }
}
