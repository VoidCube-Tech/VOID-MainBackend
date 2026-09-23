package com.voidcube.backend.core.context;

import com.voidcube.backend.core.exception.BusinessException;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

public final class CompanyContextHolder {

    private static final ThreadLocal<UUID> CURRENT_COMPANY = new ThreadLocal<>();

    private CompanyContextHolder() {
    }

    public static void setCompanyId(UUID companyId) {
        CURRENT_COMPANY.set(companyId);
    }

    public static Optional<UUID> getCompanyId() {
        return Optional.ofNullable(CURRENT_COMPANY.get());
    }

    public static UUID requireCompanyId() {
        return getCompanyId().orElseThrow(() ->
                new BusinessException("Contexto da empresa (X-Company-ID) é obrigatório para esta operação.", HttpStatus.BAD_REQUEST)
        );
    }

    public static void clear() {
        CURRENT_COMPANY.remove();
    }
}