package com.example.demo.global.datasource.config;

import com.example.demo.global.datasource.enums.ShardingStrategy;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ShardingProperty {
    private ShardingStrategy strategy;
    private List<ShardingRule> rules;
    private int mod;

    @Getter
    @Setter
    public static class ShardingRule {
        private int shardNo;
        private long rangeMin;
        private long rangeMax;
    }
}
