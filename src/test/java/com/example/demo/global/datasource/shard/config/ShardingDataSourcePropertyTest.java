package com.example.demo.global.datasource.shard.config;

import com.example.demo.global.datasource.shard.enums.ShardingStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShardingDataSourcePropertyTest {

    @Test
    void rejectsMissingMode() {
        ShardingDataSourceProperty property = new ShardingDataSourceProperty();

        assertThatThrownBy(property::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("datasource.history.mode is required");
    }

    @Test
    void rejectsModeThatDoesNotMatchDeploymentExpectation() {
        ShardingDataSourceProperty property = singleProperty();
        property.setExpectedMode(ShardingDataSourceProperty.Mode.SHARDED);

        assertThatThrownBy(property::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must match expected-mode");
    }

    @Test
    void rejectsMissingDeploymentExpectation() {
        ShardingDataSourceProperty property = singleProperty();
        property.setExpectedMode(null);

        assertThatThrownBy(property::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("datasource.history.expected-mode is required");
    }

    @Test
    void acceptsValidModularConfiguration() {
        ShardingDataSourceProperty property = shardedProperty(ShardingStrategy.MODULAR);

        property.validate();
    }

    @Test
    void rejectsDuplicateShardUrls() {
        ShardingDataSourceProperty property = shardedProperty(ShardingStrategy.MODULAR);
        property.getShards().get(1).getMaster().setUrl("jdbc:postgresql://localhost/shard-0");

        assertThatThrownBy(property::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("datasource.history.shards must use distinct URLs");
    }

    @Test
    void rejectsOverlappingRangeRules() {
        ShardingDataSourceProperty property = shardedProperty(ShardingStrategy.RANGE);
        property.getShard().setRules(List.of(rule(0, 0, 100), rule(1, 100, 200)));

        assertThatThrownBy(property::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("datasource.history.shard.rules must not overlap");
    }

    private static ShardingDataSourceProperty singleProperty() {
        ShardingDataSourceProperty property = new ShardingDataSourceProperty();
        property.setMode(ShardingDataSourceProperty.Mode.SINGLE);
        property.setExpectedMode(ShardingDataSourceProperty.Mode.SINGLE);

        DataSourceProperty single = new DataSourceProperty();
        single.setUsername("user");
        single.setPassword("password");
        single.setUrl("jdbc:postgresql://localhost/single");
        property.setSingle(single);
        return property;
    }

    private static ShardingDataSourceProperty shardedProperty(ShardingStrategy strategy) {
        ShardingDataSourceProperty property = new ShardingDataSourceProperty();
        property.setMode(ShardingDataSourceProperty.Mode.SHARDED);
        property.setExpectedMode(ShardingDataSourceProperty.Mode.SHARDED);
        property.setShards(List.of(shard("shard-0"), shard("shard-1")));

        ShardingProperty sharding = new ShardingProperty();
        sharding.setStrategy(strategy);
        if (strategy == ShardingStrategy.RANGE) {
            sharding.setRules(List.of(rule(0, 0, 99), rule(1, 100, 199)));
        }
        property.setShard(sharding);
        return property;
    }

    private static ShardingDataSourceProperty.Shard shard(String name) {
        ShardingDataSourceProperty.Shard shard = new ShardingDataSourceProperty.Shard();
        shard.setUsername("user");
        shard.setPassword("password");

        ShardingDataSourceProperty.Property master = new ShardingDataSourceProperty.Property();
        master.setName(name);
        master.setUrl("jdbc:postgresql://localhost/" + name);
        shard.setMaster(master);
        return shard;
    }

    private static ShardingProperty.ShardingRule rule(int shardNo, long min, long max) {
        ShardingProperty.ShardingRule rule = new ShardingProperty.ShardingRule();
        rule.setShardNo(shardNo);
        rule.setRangeMin(min);
        rule.setRangeMax(max);
        return rule;
    }
}
