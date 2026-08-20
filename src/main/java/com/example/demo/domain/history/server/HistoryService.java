package com.example.demo.domain.history.server;

import com.example.demo.domain.history.domain.HistoryEntity;
import com.example.demo.domain.history.port.LoadHistoryPort;
import com.example.demo.domain.history.port.SaveHistoryPort;
import com.example.demo.domain.user.domain.UserEntity;
import com.example.demo.domain.user.port.LoadUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final LoadHistoryPort loadHistoryPort;
    private final SaveHistoryPort saveHistoryPort;
    private final LoadUserPort loadUserPort;


    public HistoryEntity getHistory(long userId, long historyId) {
        return loadHistoryPort.loadHistoryId(userId, historyId);
    }

    public void save(long userId, String action) {
        UserEntity user = loadUserPort.loadUserById(userId);
        saveHistoryPort.save(userId, new HistoryEntity(action, userId));
    }
}
