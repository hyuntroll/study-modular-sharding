package com.example.demo.adapter.out.persistence.history;

import com.example.demo.application.port.out.history.LoadHistoryPort;
import com.example.demo.application.port.out.history.SaveHistoryPort;
import com.example.demo.global.datasource.aop.Sharding;
import com.example.demo.global.datasource.enums.ShardingTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(
        readOnly = true,
        transactionManager = "historyTransactionManager"
)
@Sharding(target = ShardingTarget.HISTORY)
public class HistoryPersistenceAdapter
implements
        LoadHistoryPort,
        SaveHistoryPort
{
    private final HistoryRepository historyRepository;

    @Override
    public HistoryEntity loadHistoryId(Long userId, Long historyId) {
        return historyRepository.findAllByUserIdAndId(historyId, userId)
                .stream().findFirst().orElseThrow(() -> new RuntimeException("Histroy"));
    }

    @Override
    @Transactional
    public void save(Long userId, HistoryEntity history) {
        historyRepository.save(history);
    }
}
