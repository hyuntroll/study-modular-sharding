package com.example.demo.global.datasource.shard.config;

import com.example.demo.global.datasource.shard.enums.ShardingStrategy;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ShardingProperty {
    private ShardingStrategy strategy;
    private List<ShardingRule> rules;

    @Getter
    @Setter
    public static class ShardingRule {
        private int shardNo;
        private long rangeMin;
        private long rangeMax;
    }
}
