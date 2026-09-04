package com.example.demo.global.datasource.shard.holder;


import com.example.demo.global.datasource.shard.enums.ShardingTarget;
import lombok.Getter;

import java.util.ArrayDeque;
import java.util.Deque;

public final class UserContextHolder {

    private static final ThreadLocal<Deque<Sharding>> USER_CONTEXT =
            ThreadLocal.withInitial(ArrayDeque::new);

    private UserContextHolder() {
    }

    public static void push(ShardingTarget target, long shardKey) {
        getUserContext().push(new Sharding(target, shardKey));
    }

    public static void pop() {
        Deque<Sharding> context = getUserContext();
        if (!context.isEmpty()) {
            context.pop();
        }
        if (context.isEmpty()) {
            USER_CONTEXT.remove();
        }
    }


    public static Sharding getSharding() {
        Deque<Sharding> stack = getUserContext();
        return stack.isEmpty()
                ? null
                : stack.peek();
    }

    private static Deque<Sharding> getUserContext() {
        return USER_CONTEXT.get();
    }

    @Getter
    public static class Sharding {
        private final ShardingTarget target;
        private final long shardKey;

        Sharding(ShardingTarget target, long shardKey) {
            this.target = target;
            this.shardKey = shardKey;
        }
    }
}
