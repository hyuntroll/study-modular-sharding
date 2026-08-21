package com.example.demo.application.port.out.user;

import com.example.demo.adapter.out.persistence.application.user.UserEntity;

public interface SaveUserPort {
    void save(UserEntity userEntity);
}
