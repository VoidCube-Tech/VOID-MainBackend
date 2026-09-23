package com.voidcube.backend.core.context;

import com.voidcube.backend.core.security.support.SupportSessionContext;

import java.util.Optional;
import java.util.UUID;

public final class SupportContextHolder {

    private static final ThreadLocal<SupportSessionContext> CURRENT_SUPPORT_SESSION = new ThreadLocal<>();

    private SupportContextHolder() {
    }

    public static void setSession(SupportSessionContext sessionContext) {
        CURRENT_SUPPORT_SESSION.set(sessionContext);
    }

    public static Optional<SupportSessionContext> getSession() {
        return Optional.ofNullable(CURRENT_SUPPORT_SESSION.get());
    }

    public static boolean isSupportModeActive() {
        SupportSessionContext session = CURRENT_SUPPORT_SESSION.get();
        return session != null && !session.isExpired();
    }

    public static void clear() {
        CURRENT_SUPPORT_SESSION.remove();
    }
}