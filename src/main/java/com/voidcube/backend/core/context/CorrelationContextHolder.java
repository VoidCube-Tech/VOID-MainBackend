package com.voidcube.backend.core.context;

import com.voidcube.backend.core.utils.UuidUtils;
import org.springframework.util.StringUtils;

public final class CorrelationContextHolder {

    private static final ThreadLocal<String> CURRENT_CORRELATION_ID = new ThreadLocal<>();

    private CorrelationContextHolder() {
    }

    public static void setCorrelationId(String correlationId) {
        CURRENT_CORRELATION_ID.set(correlationId);
    }

    public static String getCorrelationId() {
        return CURRENT_CORRELATION_ID.get();
    }

    public static String getOrGenerate() {
        String id = CURRENT_CORRELATION_ID.get();
        if (!StringUtils.hasText(id)) {
            id = UuidUtils.generateV7().toString();
            CURRENT_CORRELATION_ID.set(id);
        }
        return id;
    }

    public static void clear() {
        CURRENT_CORRELATION_ID.remove();
    }
}