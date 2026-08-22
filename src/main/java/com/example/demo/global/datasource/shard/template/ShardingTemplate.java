package com.example.demo.global.datasource.shard.template;


import com.example.demo.global.datasource.shard.config.ShardingScope;
import com.example.demo.global.datasource.shard.enums.ShardingTarget;

import java.util.function.Supplier;

public class ShardingTemplate {

    public static <T> T execute(
            ShardingTarget target,
            Long shardKey,
            Supplier<T> action
    ) {
        try (ShardingScope ignored = ShardingScope.open(target, shardKey)) {
            return action.get();
        }
    }

    public static void execute(
            ShardingTarget target,
            Long shardKey,
            Runnable action
    ) {
        try (ShardingScope ignored = ShardingScope.open(target, shardKey)) {
            action.run();
        }
    }
}
