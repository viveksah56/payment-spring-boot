package com.backend.Service;

import com.backend.Payload.Request.UserRegister;
import com.backend.Payload.Respone.UserResponse;

public interface UserService {
    UserResponse register(UserRegister userRegister);
}
