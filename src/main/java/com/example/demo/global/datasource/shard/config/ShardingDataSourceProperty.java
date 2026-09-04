package com.example.demo.global.datasource.shard.config;

import com.example.demo.global.datasource.shard.enums.ShardingStrategy;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
public class ShardingDataSourceProperty {
    private List<Shard> shards;

    private Mode mode;

    private Mode expectedMode;

    private DataSourceProperty single;

    private ShardingProperty shard;

    public void validate() {
        require(mode != null, "datasource.history.mode is required");
        require(expectedMode != null, "datasource.history.expected-mode is required");
        require(mode == expectedMode,
                "datasource.history.mode must match expected-mode: " + expectedMode);

        if (mode == Mode.SINGLE) {
            validateDataSource(single, "datasource.history.single");
            return;
        }

        require(shard != null, "datasource.history.shard is required in SHARDED mode");
        require(shard.getStrategy() != null,
                "datasource.history.shard.strategy is required in SHARDED mode");
        require(shards != null && shards.size() >= 2,
                "datasource.history.shards must contain at least two entries in SHARDED mode");

        Set<String> urls = new HashSet<>();
        for (int i = 0; i < shards.size(); i++) {
            Shard current = shards.get(i);
            require(current != null, "datasource.history.shards[" + i + "] is required");
            requireNonBlank(current.username, "datasource.history.shards[" + i + "].username");
            requireNonBlank(current.password, "datasource.history.shards[" + i + "].password");
            require(current.master != null,
                    "datasource.history.shards[" + i + "].master is required");
            requireNonBlank(current.master.name,
                    "datasource.history.shards[" + i + "].master.name");
            requireNonBlank(current.master.url,
                    "datasource.history.shards[" + i + "].master.url");
            require(urls.add(current.master.url),
                    "datasource.history.shards must use distinct URLs");
        }

        if (shard.getStrategy() == ShardingStrategy.RANGE) {
            validateRangeRules();
        }
    }

    private void validateRangeRules() {
        List<ShardingProperty.ShardingRule> rules = shard.getRules();
        require(rules != null && !rules.isEmpty(),
                "datasource.history.shard.rules is required for RANGE strategy");

        for (int i = 0; i < rules.size(); i++) {
            ShardingProperty.ShardingRule rule = rules.get(i);
            require(rule != null, "datasource.history.shard.rules[" + i + "] is required");
            require(rule.getRangeMin() <= rule.getRangeMax(),
                    "range-min must be less than or equal to range-max");
            require(rule.getShardNo() >= 0 && rule.getShardNo() < shards.size(),
                    "range rule references an unknown shard: " + rule.getShardNo());

            for (int j = 0; j < i; j++) {
                ShardingProperty.ShardingRule previous = rules.get(j);
                boolean overlaps = rule.getRangeMin() <= previous.getRangeMax()
                        && previous.getRangeMin() <= rule.getRangeMax();
                require(!overlaps, "datasource.history.shard.rules must not overlap");
            }
        }
    }

    private static void validateDataSource(DataSourceProperty property, String path) {
        require(property != null, path + " is required in SINGLE mode");
        requireNonBlank(property.getUsername(), path + ".username");
        requireNonBlank(property.getPassword(), path + ".password");
        requireNonBlank(property.getUrl(), path + ".url");
    }

    private static void requireNonBlank(String value, String path) {
        require(value != null && !value.isBlank(), path + " is required");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    @Getter
    @Setter
    public static class Shard {
        private String username;
        private String password;
        private Property master;
    }

    @Getter
    @Setter
    public static class Property {
        private String name;
        private String url;
    }

    public enum Mode {
        SINGLE,
        SHARDED
    }
}
