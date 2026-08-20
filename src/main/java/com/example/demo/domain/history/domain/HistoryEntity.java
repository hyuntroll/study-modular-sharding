package com.example.demo.domain.history.domain;

import com.example.demo.domain.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "history")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class HistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public HistoryEntity(String action, Long userId) {
        this.action = action;
        this.userId = userId;
    }
}
