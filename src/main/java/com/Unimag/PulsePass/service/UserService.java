package com.Unimag.PulsePass.service;

import com.Unimag.PulsePass.dto.request.RegisterUserRequest;
import com.Unimag.PulsePass.dto.response.UserResponse;

public interface UserService {
    UserResponse register(RegisterUserRequest request);
    UserResponse findByEmail(String email);
    UserResponse findByUsername(String username);
}
