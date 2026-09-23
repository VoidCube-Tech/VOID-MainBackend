package com.voidcube.backend.core.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationContextHolderTest {

    @AfterEach
    void tearDown() {
        CorrelationContextHolder.clear();
    }

    @Test
    @DisplayName("Deve definir e recuperar correlationId")
    void setAndGet_Success() {
        CorrelationContextHolder.setCorrelationId("test-corr-id-123");
        assertThat(CorrelationContextHolder.getCorrelationId()).isEqualTo("test-corr-id-123");
    }

    @Test
    @DisplayName("Deve gerar correlationId caso não exista")
    void getOrGenerate_GeneratesWhenEmpty() {
        String generated = CorrelationContextHolder.getOrGenerate();
        assertThat(generated).isNotBlank();
        assertThat(CorrelationContextHolder.getCorrelationId()).isEqualTo(generated);
    }

    @Test
    @DisplayName("Deve limpar correlationId no clear()")
    void clear_Success() {
        CorrelationContextHolder.setCorrelationId("test-id");
        CorrelationContextHolder.clear();
        assertThat(CorrelationContextHolder.getCorrelationId()).isNull();
    }
}