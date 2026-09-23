package com.voidcube.backend.core.security.support;

import com.voidcube.backend.core.context.SupportContextHolder;
import com.voidcube.backend.core.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SupportModeEvaluator {

    public void validateSupportAccess(UUID targetCompanyId) {
        if (!SupportContextHolder.isSupportModeActive()) {
            return;
        }

        SupportSessionContext session = SupportContextHolder.getSession()
                .orElseThrow(() -> new BusinessException("Sessão de suporte inválida ou inexistente.", HttpStatus.FORBIDDEN));

        if (session.isExpired()) {
            throw new BusinessException("A sessão de suporte para esta empresa expirou.", HttpStatus.FORBIDDEN);
        }

        if (!session.companyId().equals(targetCompanyId)) {
            throw new BusinessException("A sessão de suporte ativa não confere autorização para a empresa informada.", HttpStatus.FORBIDDEN);
        }
    }
}