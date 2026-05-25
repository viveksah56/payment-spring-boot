package com.backend.Service.Impl;

import com.backend.Entity.User;
import com.backend.Mapper.UserMapper;
import com.backend.Payload.Request.UserRegister;
import com.backend.Payload.Respone.UserResponse;
import com.backend.Repository.UserRepository;
import com.backend.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    @Override
    public UserResponse register(UserRegister userRegister) {
        User user = User.builder()
                .email(userRegister.email())
                .password(passwordEncoder.encode(userRegister.password()))
                .fullName(userRegister.fullName())
                .build();
        User saved = userRepository.save(user);
        return mapper.toResponse(saved);

    }
}
