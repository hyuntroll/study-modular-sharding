package com.example.demo.domain.user.adapter;

import com.example.demo.domain.user.domain.UserEntity;
import com.example.demo.domain.user.port.LoadUserPort;
import com.example.demo.domain.user.port.SaveUserPort;
import com.example.demo.global.datasource.aop.Sharding;
import com.example.demo.global.datasource.enums.ShardingTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
//@Sharding(target = ShardingTarget.HISTORY)
public class UserPersistenceAdapter
    implements LoadUserPort,
        SaveUserPort
{
    private final UserRepository userRepository;

    @Override
    public UserEntity loadUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }


    @Override
    @Transactional
    public void save(UserEntity userEntity) {
        userRepository.save(userEntity);
    }
}
