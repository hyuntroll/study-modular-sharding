package com.example.demo.domain.user.port;

import com.example.demo.domain.user.domain.UserEntity;

public interface SaveUserPort {
    void save(UserEntity userEntity);
}
