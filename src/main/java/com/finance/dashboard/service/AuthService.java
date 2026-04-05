package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.LoginRequest;
import com.finance.dashboard.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
}
