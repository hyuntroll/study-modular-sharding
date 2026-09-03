package com.example.demo.adapter.out.persistence.history;

import com.example.demo.application.port.out.history.LoadHistoryPort;
import com.example.demo.application.port.out.history.SaveHistoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.example.demo.global.datasource.shard.enums.ShardingTarget.HISTORY;
import static com.example.demo.global.datasource.shard.template.ShardingTemplate.execute;

@Component
@RequiredArgsConstructor
@Transactional(
        readOnly = true,
        transactionManager = "historyTransactionManager"
)
@ConditionalOnProperty(
        prefix = "sharding",
        name = "mode",
        havingValue = "template"
)
public class HistoryPersistenceAdapterTemplateVer
implements
        LoadHistoryPort,
        SaveHistoryPort
{
    private final HistoryRepository historyRepository;

    @Override
    public HistoryEntity loadHistoryId(Long userId, Long historyId) {
        return execute(HISTORY, userId, () ->
            historyRepository.findAllByUserIdAndId(userId, historyId)
                    .stream().findFirst().orElseThrow(() -> new RuntimeException("Histroy"))
        );
    }

    @Override
    @Transactional
    public void save(Long userId, HistoryEntity history) {
        execute(HISTORY, userId, () ->
            historyRepository.save(history)
        );
    }
}
