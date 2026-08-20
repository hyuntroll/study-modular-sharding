package com.example.demo.domain.history.port;


import com.example.demo.domain.history.domain.HistoryEntity;

public interface LoadHistoryPort {
    HistoryEntity loadHistoryId(Long userId, Long historyId);
}
