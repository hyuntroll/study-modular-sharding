package com.example.demo.application.port.out.history;


import com.example.demo.adapter.out.persistence.history.HistoryEntity;

public interface SaveHistoryPort {
    void save(Long userId, HistoryEntity history);
}
