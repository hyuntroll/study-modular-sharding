package com.example.demo.domain.user.service;

import com.example.demo.domain.user.domain.UserEntity;
import com.example.demo.domain.user.port.LoadUserPort;
import com.example.demo.domain.user.port.SaveUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;

    public void create(String email, String password) {
        saveUserPort.save(
                new UserEntity(email, password)
        );
    }

    public UserEntity get(long userId) {
        return loadUserPort.loadUserById(userId);
    }
}
