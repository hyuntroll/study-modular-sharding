package com.example.demo.global.datasource.shard.router;

import com.example.demo.global.datasource.shard.config.ShardingProperty;
import com.example.demo.global.datasource.shard.config.ShardingScope;
import com.example.demo.global.datasource.shard.enums.ShardingStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.demo.global.datasource.shard.enums.ShardingTarget.HISTORY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataSourceRouterTest {

    @Test
    void routesModularKeysUsingConfiguredShardCount() {
        TestRouter router = new TestRouter(property(ShardingStrategy.MODULAR), 2);

        try (ShardingScope ignored = ShardingScope.open(HISTORY, 3L)) {
            assertThat(router.lookupKey()).isEqualTo(1);
        }
    }

    @Test
    void failsWithoutShardingKey() {
        TestRouter router = new TestRouter(property(ShardingStrategy.MODULAR), 2);

        assertThatThrownBy(router::lookupKey)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Sharding key is required");
    }

    @Test
    void failsWhenNoRangeMatches() {
        ShardingProperty property = property(ShardingStrategy.RANGE);
        property.setRules(List.of(rule(0, 0, 99), rule(1, 100, 199)));
        TestRouter router = new TestRouter(property, 2);

        try (ShardingScope ignored = ShardingScope.open(HISTORY, 200L)) {
            assertThatThrownBy(router::lookupKey)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("No shard range found for key: 200");
        }
    }

    private static ShardingProperty property(ShardingStrategy strategy) {
        ShardingProperty property = new ShardingProperty();
        property.setStrategy(strategy);
        return property;
    }

    private static ShardingProperty.ShardingRule rule(int shardNo, long min, long max) {
        ShardingProperty.ShardingRule rule = new ShardingProperty.ShardingRule();
        rule.setShardNo(shardNo);
        rule.setRangeMin(min);
        rule.setRangeMax(max);
        return rule;
    }

    private static final class TestRouter extends DataSourceRouter {
        private TestRouter(ShardingProperty property, int shardCount) {
            super(property, shardCount);
        }

        private Object lookupKey() {
            return determineCurrentLookupKey();
        }
    }
}
