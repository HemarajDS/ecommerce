package com.mercora.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mercora.auth.config.LoginAttemptService;
import com.mercora.auth.dto.LoginRequest;
import com.mercora.auth.dto.RegisterRequest;
import com.mercora.auth.dto.TokenResponse;
import com.mercora.auth.exception.BusinessRuleException;
import com.mercora.auth.model.Role;
import com.mercora.auth.model.UserAccount;
import com.mercora.auth.repository.UserAccountRepository;
import com.mercora.auth.service.AuditLogService;
import com.mercora.auth.service.JwtService;
import com.mercora.auth.service.RefreshTokenService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserAccount user;

    @BeforeEach
    void setUp() {
        user = new UserAccount();
        user.setId("user-1");
        user.setFirstName("Asha");
        user.setLastName("Patel");
        user.setEmail("asha@example.com");
        user.setPasswordHash("hashed");
        user.setRole(Role.CUSTOMER);
        user.setCreatedAt(Instant.now());
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        when(userAccountRepository.existsByEmail("asha@example.com")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () ->
                authService.register(new RegisterRequest("Asha", "Patel", "asha@example.com", "Password1", Role.CUSTOMER)));

        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void loginShouldReturnTokens() {
        when(loginAttemptService.isLocked("asha@example.com")).thenReturn(false);
        when(userAccountRepository.findByEmail("asha@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1", "hashed")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(refreshTokenService.create("user-1")).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpiry()).thenReturn(Instant.parse("2026-03-24T10:00:00Z"));
        when(jwtService.getRefreshTokenExpiry()).thenReturn(Instant.parse("2026-03-31T10:00:00Z"));

        TokenResponse response = authService.login(new LoginRequest("asha@example.com", "Password1"));

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("asha@example.com", response.user().email());
    }
}
