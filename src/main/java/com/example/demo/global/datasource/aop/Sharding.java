package com.example.demo.global.datasource.aop;

import com.example.demo.global.datasource.enums.ShardingTarget;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Sharding {
    ShardingTarget target();
}
