package com.example.demo.application.user;

import com.example.demo.adapter.out.persistence.application.user.UserEntity;
import com.example.demo.application.port.out.user.LoadUserPort;
import com.example.demo.application.port.out.user.SaveUserPort;
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
