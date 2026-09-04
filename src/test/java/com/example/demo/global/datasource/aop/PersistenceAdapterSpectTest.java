package com.example.demo.global.datasource.aop;

import com.example.demo.global.datasource.shard.enums.ShardingTarget;
import com.example.demo.global.datasource.shard.holder.UserContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PersistenceAdapterSpectTest {

    private final PersistenceAdapterSpect aspect = new PersistenceAdapterSpect();

    @Test
    void opensAndClosesShardingContextAroundInvocation() throws Throwable {
        ProceedingJoinPoint joinPoint = joinPoint(3L);
        when(joinPoint.proceed()).thenAnswer(ignored -> {
            assertThat(UserContextHolder.getSharding().getShardKey()).isEqualTo(3L);
            return "result";
        });

        assertThat(aspect.route(joinPoint)).isEqualTo("result");
        assertThat(UserContextHolder.getSharding()).isNull();
    }

    @Test
    void rejectsMethodWithoutLongShardKeyAsFirstArgument() {
        ProceedingJoinPoint joinPoint = joinPoint("wrong");

        assertThatThrownBy(() -> aspect.route(joinPoint))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("@Sharding methods require a Long shard key as the first argument");
    }

    private static ProceedingJoinPoint joinPoint(Object firstArgument) {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getTarget()).thenReturn(new ShardedTarget());
        when(joinPoint.getArgs()).thenReturn(new Object[]{firstArgument});
        return joinPoint;
    }

    @Sharding(target = ShardingTarget.HISTORY)
    private static class ShardedTarget {
    }
}
