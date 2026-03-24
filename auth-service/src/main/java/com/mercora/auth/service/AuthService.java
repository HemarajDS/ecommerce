package com.mercora.auth.service;

import com.mercora.auth.dto.LoginRequest;
import com.mercora.auth.dto.RefreshTokenRequest;
import com.mercora.auth.dto.RegisterRequest;
import com.mercora.auth.dto.TokenResponse;
import com.mercora.auth.dto.UserProfileResponse;

public interface AuthService {

    UserProfileResponse register(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse refreshToken(RefreshTokenRequest request);

    UserProfileResponse getProfile(String userId);
}
