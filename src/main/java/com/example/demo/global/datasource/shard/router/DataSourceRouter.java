package com.example.demo.global.datasource.shard.router;

import com.example.demo.global.datasource.shard.config.ShardingProperty;
import com.example.demo.global.datasource.shard.holder.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.demo.global.datasource.config.DataSourceConfig.SHARD_DELIMITER;

@Slf4j
@RequiredArgsConstructor
public class DataSourceRouter extends AbstractRoutingDataSource {
    private Map<Integer, String> shards;
    private final ShardingProperty property;

    @Override
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        super.setTargetDataSources(targetDataSources);

        shards = new HashMap<>();

        for(Object item: targetDataSources.keySet()) {
            String dataSourceName = item.toString();
            String shardNoStr = dataSourceName.split(SHARD_DELIMITER)[0];
            shards.put(Integer.parseInt(shardNoStr), dataSourceName);
        }
    }

    @Override
    protected Object determineCurrentLookupKey() {
        int shardNo = getShardNo(
                UserContextHolder.getSharding()
        );
        log.info(shards.get(shardNo));
        return shards.get(shardNo);
    }

    private int getShardNo(UserContextHolder.Sharding sharding) {
        if (sharding == null) {
            return 0;
        }
        return switch (property.getStrategy()) {
            case RANGE -> getShardNoByRange(property.getRules(), sharding.getShardKey());
            case MODULAR -> getShardNoByModular(property.getMod(), sharding.getShardKey());
        };
    }

    private int getShardNoByRange(List<ShardingProperty.ShardingRule> rules, long shardKey) {
        for (ShardingProperty.ShardingRule rule : rules) {
            if (rule.getRangeMin() <= shardKey && shardKey <= rule.getRangeMax()) {
                return rule.getShardNo();
            }
        }

        return 0;
    }

    private int getShardNoByModular(int modulus, long shardKey) {
        return (int) (shardKey % modulus);
    }
}
