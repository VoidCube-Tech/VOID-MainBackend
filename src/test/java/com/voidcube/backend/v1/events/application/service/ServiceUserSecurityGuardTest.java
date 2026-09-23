package com.voidcube.backend.v1.events.application.service;

import com.voidcube.backend.core.exception.BusinessException;
import com.voidcube.backend.core.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceUserSecurityGuardTest {

    private final ServiceUserSecurityGuard guard = new ServiceUserSecurityGuard();

    @Test
    @DisplayName("Deve lançar PASSWORD_CHANGE_REQUIRED quando temporaryPassword for true")
    void guard_ThrowsPasswordChangeRequired_WhenTemporaryPasswordIsTrue() {
        UserPrincipal principal = new UserPrincipal(
                UUID.randomUUID(),
                "usuario@empresa.com",
                UUID.randomUUID(),
                true,
                "SERVICE_USER"
        );

        assertThatThrownBy(() -> guard.ensurePasswordChangeNotRequired(principal))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("PASSWORD_CHANGE_REQUIRED");
                    assertThat(be.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                });
    }

    @Test
    @DisplayName("Não deve lançar exceção quando temporaryPassword for false")
    void guard_Passes_WhenTemporaryPasswordIsFalse() {
        UserPrincipal principal = new UserPrincipal(
                UUID.randomUUID(),
                "usuario@empresa.com",
                UUID.randomUUID(),
                false,
                "SERVICE_USER"
        );

        assertThatCode(() -> guard.ensurePasswordChangeNotRequired(principal))
                .doesNotThrowAnyException();
    }
}