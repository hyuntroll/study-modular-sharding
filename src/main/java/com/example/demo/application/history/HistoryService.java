package com.example.demo.application.history;

import com.example.demo.adapter.out.persistence.history.HistoryEntity;
import com.example.demo.application.port.out.history.LoadHistoryPort;
import com.example.demo.application.port.out.history.SaveHistoryPort;
import com.example.demo.adapter.out.persistence.application.user.UserEntity;
import com.example.demo.application.port.out.user.LoadUserPort;
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
