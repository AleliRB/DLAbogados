package com.dlabogados.auth.service;

import com.dlabogados.auth.dto.AuthResponse;
import com.dlabogados.auth.dto.LoginRequest;
import com.dlabogados.auth.dto.RegisterClientRequest;
import com.dlabogados.auth.dto.UserResponse;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest);
    UserResponse registerClient(RegisterClientRequest registerRequest);
    UserResponse getCurrentUser(String email);
}
