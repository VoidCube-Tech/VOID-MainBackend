package com.voidcube.backend.core.security.support;

import java.time.Instant;
import java.util.UUID;

public record SupportSessionContext(
        UUID sessionId,
        UUID operatorId,
        UUID companyId,
        boolean isEmergency,
        String reason,
        Instant expiresAt
) {
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}