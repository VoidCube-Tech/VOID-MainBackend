package com.voidcube.backend.v1.events.application.service;

import com.voidcube.backend.core.exception.BusinessException;
import com.voidcube.backend.core.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ServiceUserSecurityGuard {

    public void ensurePasswordChangeNotRequired(UserPrincipal principal) {
        if (principal != null && principal.temporaryPassword()) {
            throw new BusinessException(
                    "PASSWORD_CHANGE_REQUIRED",
                    "É necessário redefinir sua senha temporária antes de realizar esta operação.",
                    HttpStatus.FORBIDDEN
            );
        }
    }
}