package com.example.demo.global.datasource.aop;

import com.example.demo.global.datasource.holder.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class PersistenceAdapterSpect {

    @Pointcut("execution(" +
            "public * " + // public 메서드, 반환 타입 상관 없음
            "com.example.demo.domain.*.adapter..*.*(..))" + // ..* 패키지 하위에 * 모든 메서드(..) 모든 파라미터
            "")
    private void persistenceAdapter() {}

    @Around("persistenceAdapter() && @within(sharding) && args(shardKey,..)")
    public Object route(
            ProceedingJoinPoint joinPoint,
            Sharding sharding,
            Long shardKey
    ) throws Throwable {
        log.info(String.valueOf(shardKey));

        UserContextHolder.setSharding(sharding.target(), shardKey);
        try {
            return joinPoint.proceed();
        } finally {
            UserContextHolder.clearSharding();
        }
    }
}
