package com.example.demo.global.datasource.shard.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ShardingDataSourceProperty {
    private List<Shard> shards;

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
}
