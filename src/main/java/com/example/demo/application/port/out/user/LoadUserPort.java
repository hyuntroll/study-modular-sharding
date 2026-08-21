package com.example.demo.application.port.out.user;

import com.example.demo.adapter.out.persistence.application.user.UserEntity;

public interface LoadUserPort {
    UserEntity loadUserById(Long userId);
}
