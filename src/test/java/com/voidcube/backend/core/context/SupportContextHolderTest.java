package com.voidcube.backend.core.context;

import com.voidcube.backend.core.security.support.SupportSessionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SupportContextHolderTest {

    @AfterEach
    void tearDown() {
        SupportContextHolder.clear();
    }

    @Test
    @DisplayName("Deve armazenar e recuperar a sessão de suporte ativa")
    void setSessionAndRetrieve_Success() {
        UUID sessionId = UUID.randomUUID();
        UUID supportUserId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        SupportSessionContext context = new SupportSessionContext(
                sessionId,
                supportUserId,
                companyId,
                false,
                "Investigação de suporte",
                Instant.now().plus(1, ChronoUnit.HOURS)
        );

        SupportContextHolder.setSession(context);

        assertThat(SupportContextHolder.getSession()).isPresent().contains(context);
        assertThat(SupportContextHolder.isSupportModeActive()).isTrue();
        assertThat(SupportContextHolder.getSession().get().isEmergency()).isFalse();
    }

    @Test
    @DisplayName("Deve identificar sessão de emergência corretamente")
    void emergencySession_IdentifiedCorrectly() {
        SupportSessionContext context = new SupportSessionContext(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                true,
                "Emergência de produção",
                Instant.now().plus(1, ChronoUnit.HOURS)
        );

        SupportContextHolder.setSession(context);

        assertThat(SupportContextHolder.isSupportModeActive()).isTrue();
        assertThat(SupportContextHolder.getSession().get().isEmergency()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar falso para sessão expirada")
    void expiredSession_ReturnsInactive() {
        SupportSessionContext context = new SupportSessionContext(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                false,
                "Sessão antiga",
                Instant.now().minus(10, ChronoUnit.MINUTES)
        );

        SupportContextHolder.setSession(context);

        assertThat(SupportContextHolder.isSupportModeActive()).isFalse();
    }

    @Test
    @DisplayName("Deve limpar a sessão ao chamar clear()")
    void clear_RemovesSession() {
        SupportSessionContext context = new SupportSessionContext(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                false,
                "Teste",
                Instant.now().plus(1, ChronoUnit.HOURS)
        );

        SupportContextHolder.setSession(context);
        SupportContextHolder.clear();

        assertThat(SupportContextHolder.getSession()).isEmpty();
        assertThat(SupportContextHolder.isSupportModeActive()).isFalse();
    }
}