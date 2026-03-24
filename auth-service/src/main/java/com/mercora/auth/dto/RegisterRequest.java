package com.mercora.auth.dto;

import com.mercora.auth.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$",
                message = "Password must be at least 8 characters and include upper, lower, and numeric characters")
        String password,
        @NotNull Role role) {
}
