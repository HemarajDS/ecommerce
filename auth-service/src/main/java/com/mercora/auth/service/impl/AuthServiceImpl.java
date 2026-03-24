package com.mercora.auth.service.impl;

import com.mercora.auth.config.LoginAttemptService;
import com.mercora.auth.dto.LoginRequest;
import com.mercora.auth.dto.RefreshTokenRequest;
import com.mercora.auth.dto.RegisterRequest;
import com.mercora.auth.dto.TokenResponse;
import com.mercora.auth.dto.UserProfileResponse;
import com.mercora.auth.exception.BusinessRuleException;
import com.mercora.auth.exception.ResourceNotFoundException;
import com.mercora.auth.exception.UnauthorizedException;
import com.mercora.auth.model.AuditAction;
import com.mercora.auth.model.UserAccount;
import com.mercora.auth.repository.UserAccountRepository;
import com.mercora.auth.service.AuditLogService;
import com.mercora.auth.service.AuthService;
import com.mercora.auth.service.JwtService;
import com.mercora.auth.service.RefreshTokenService;
import java.time.Instant;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final AuditLogService auditLogService;

    public AuthServiceImpl(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            LoginAttemptService loginAttemptService,
            AuditLogService auditLogService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.loginAttemptService = loginAttemptService;
        this.auditLogService = auditLogService;
    }

    @Override
    public UserProfileResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userAccountRepository.existsByEmail(email)) {
            throw new BusinessRuleException("An account with this email already exists");
        }

        UserAccount user = new UserAccount();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setEmailVerified(false);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        UserAccount savedUser = userAccountRepository.save(user);
        auditLogService.record(savedUser, AuditAction.REGISTER, Map.of("role", savedUser.getRole().name()));
        return toProfile(savedUser);
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        if (loginAttemptService.isLocked(email)) {
            throw new UnauthorizedException("Account temporarily locked due to too many failed attempts");
        }

        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> invalidCredentials(email));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials(email);
        }

        loginAttemptService.reset(email);
        user.setLastLoginAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userAccountRepository.save(user);
        auditLogService.record(user, AuditAction.LOGIN, Map.of("status", "SUCCESS"));
        return issueTokens(user);
    }

    @Override
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String userId = refreshTokenService.findUserId(request.refreshToken());
        if (userId == null) {
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        refreshTokenService.revoke(request.refreshToken());
        auditLogService.record(user, AuditAction.REFRESH, Map.of("status", "SUCCESS"));
        return issueTokens(user);
    }

    @Override
    public UserProfileResponse getProfile(String userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toProfile(user);
    }

    private UnauthorizedException invalidCredentials(String email) {
        long attempts = loginAttemptService.incrementFailedAttempts(email);
        auditLogService.record(email, AuditAction.LOGIN_FAILED, Map.of("attempt", attempts));
        return new UnauthorizedException("Invalid email or password");
    }

    private TokenResponse issueTokens(UserAccount user) {
        return new TokenResponse(
                jwtService.generateAccessToken(user),
                refreshTokenService.create(user.getId()),
                "Bearer",
                jwtService.getAccessTokenExpiry(),
                jwtService.getRefreshTokenExpiry(),
                toProfile(user));
    }

    private UserProfileResponse toProfile(UserAccount user) {
        return new UserProfileResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.isEmailVerified(),
                user.getCreatedAt());
    }
}
