package com.example.demo.global.datasource.aop;

import com.example.demo.global.datasource.shard.config.ShardingScope;
import com.example.demo.global.datasource.shard.holder.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
@ConditionalOnProperty(
        prefix = "sharding",
        name = "mode",
        havingValue = "aop"
)
@RequiredArgsConstructor
public class PersistenceAdapterSpect {

    @Pointcut("execution(" +
            "public * " + // public 메서드, 반환 타입 상관 없음
            "com.example.demo.adapter.out.persistence..*.*(..))" + // ..* 패키지 하위에 * 모든 메서드(..) 모든 파라미터
            "")
    private void persistenceAdapter() {}

    @Around("persistenceAdapter() && @within(sharding) && args(shardKey,..)")
    public Object route(
            ProceedingJoinPoint joinPoint,
            Sharding sharding,
            Long shardKey
    ) throws Throwable {
        log.info(String.valueOf(shardKey));

        try (var ignored = ShardingScope.open(sharding.target(), shardKey)) {
            return joinPoint.proceed();
        }
    }
}
