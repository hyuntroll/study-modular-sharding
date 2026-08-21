package com.example.demo.adapter.out.persistence.application.user;

import com.example.demo.application.port.out.user.LoadUserPort;
import com.example.demo.application.port.out.user.SaveUserPort;
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
