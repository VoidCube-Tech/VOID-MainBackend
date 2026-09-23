package com.voidcube.backend.core.security;

import java.util.UUID;

public record UserPrincipal(
        UUID id,
        String email,
        UUID companyId,
        boolean temporaryPassword,
        String userType
) {
    public UserPrincipal(UUID id, String email) {
        this(id, email, null, false, "PLATFORM");
    }
}