package com.example.demo.global.datasource.shard.config;

import com.example.demo.global.datasource.shard.enums.ShardingTarget;
import com.example.demo.global.datasource.shard.holder.UserContextHolder;

public final class ShardingScope implements AutoCloseable {

    private boolean closed = false;

    private ShardingScope(ShardingTarget target, Long shardKey) {
        UserContextHolder.push(target, shardKey);
    }

    public static ShardingScope open(ShardingTarget target, Long shardKey) {
        return new ShardingScope(target, shardKey);
    }

    @Override
    public void close() {
        if (!closed) {
            UserContextHolder.pop();
            closed = true;
        }
    }
}
