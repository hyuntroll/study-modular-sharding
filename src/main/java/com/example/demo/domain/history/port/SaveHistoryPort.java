package com.example.demo.domain.history.port;


import com.example.demo.domain.history.domain.HistoryEntity;

public interface SaveHistoryPort {
    void save(Long userId, HistoryEntity history);
}
