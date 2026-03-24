package com.mercora.auth.dto;

import com.mercora.auth.model.Role;
import java.time.Instant;

public record UserProfileResponse(
        String id,
        String firstName,
        String lastName,
        String email,
        Role role,
        boolean emailVerified,
        Instant createdAt) {
}
