package com.example.demo.application.port.out.history;


import com.example.demo.adapter.out.persistence.history.HistoryEntity;

public interface LoadHistoryPort {
    HistoryEntity loadHistoryId(Long userId, Long historyId);
}
