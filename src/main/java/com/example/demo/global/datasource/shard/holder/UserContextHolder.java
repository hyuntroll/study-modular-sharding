package com.example.demo.global.datasource.shard.holder;


import com.example.demo.global.datasource.shard.enums.ShardingTarget;
import lombok.Getter;
import lombok.Setter;

public class UserContextHolder {

    private static final ThreadLocal<Context> USER_CONTEXT =
            ThreadLocal.withInitial(Context::new);

    public static void setSharding(ShardingTarget target, long shardKey) {
        getUserContext().setSharding(new Sharding(target, shardKey));
    }

    public static void clearSharding() {
        getUserContext().setSharding(null);
    }

    public static Sharding getSharding() {
        Context context = getUserContext();

        if (context == null) {
            return null;
        }

        return context.getSharding();
    }

    public static Context getUserContext() {
        return USER_CONTEXT.get();
    }

    @Getter
    @Setter
    public static class Context {
        private Sharding sharding;
    }

    @Getter
    @Setter
    public static class Sharding {
        private ShardingTarget target;
        private long shardKey;

        Sharding(ShardingTarget target, long shardKey) {
            this.target = target;
            this.shardKey = shardKey;
        }
    }
}
