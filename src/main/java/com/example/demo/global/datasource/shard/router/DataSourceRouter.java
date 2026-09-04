package com.example.demo.global.datasource.shard.router;

import com.example.demo.global.datasource.shard.config.ShardingProperty;
import com.example.demo.global.datasource.shard.holder.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;


import java.util.List;


@Slf4j
@RequiredArgsConstructor
public class DataSourceRouter extends AbstractRoutingDataSource {
    private final ShardingProperty property;
    private final int shardCount;

    @Override
    protected Object determineCurrentLookupKey() {
        int shardNo = getShardNo(UserContextHolder.getSharding());
        log.atDebug().addKeyValue("shard_index", shardNo).log("Routing data source");
        return shardNo;
    }

    private int getShardNo(UserContextHolder.Sharding sharding) {
        if (sharding == null) {
            throw new IllegalStateException("Sharding key is required");
        }
        return switch (property.getStrategy()) {
            case RANGE -> getShardNoByRange(property.getRules(), sharding.getShardKey());
            case MODULAR -> Math.floorMod(sharding.getShardKey(), shardCount);
        };
    }

    private int getShardNoByRange(List<ShardingProperty.ShardingRule> rules, long shardKey) {
        for (ShardingProperty.ShardingRule rule : rules) {
            if (rule.getRangeMin() <= shardKey && shardKey <= rule.getRangeMax()) {
                return rule.getShardNo();
            }
        }

        throw new IllegalStateException("No shard range found for key: " + shardKey);
    }
}
