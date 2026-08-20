package com.example.demo.domain.history.adapter;

import com.example.demo.domain.history.domain.HistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<HistoryEntity, Long> {
    List<HistoryEntity> findAllByUserIdAndId(Long userId, Long id);
}
