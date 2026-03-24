package com.mercora.auth.controller;

import com.mercora.auth.dto.ApiErrorResponse;
import com.mercora.auth.dto.LoginRequest;
import com.mercora.auth.dto.RefreshTokenRequest;
import com.mercora.auth.dto.RegisterRequest;
import com.mercora.auth.dto.TokenResponse;
import com.mercora.auth.dto.UserProfileResponse;
import com.mercora.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Authentication and account management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "409", description = "Email already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public UserProfileResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh a valid session")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public UserProfileResponse me(@RequestHeader("X-User-Id") String userId) {
        return authService.getProfile(userId);
    }
}
