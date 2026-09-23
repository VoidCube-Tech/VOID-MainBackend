package com.voidcube.backend.core.security;

import java.util.UUID;

public record UserPrincipal(
        UUID id,
        String email
) {
}