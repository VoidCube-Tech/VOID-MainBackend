package com.voidcube.backend.core.context;

import com.voidcube.backend.core.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompanyContextHolderTest {

    @AfterEach
    void tearDown() {
        CompanyContextHolder.clear();
    }

    @Test
    @DisplayName("Deve definir e recuperar companyId no ThreadLocal")
    void setAndGetCompanyId_Success() {
        UUID companyId = UUID.randomUUID();
        CompanyContextHolder.setCompanyId(companyId);

        Optional<UUID> retrieved = CompanyContextHolder.getCompanyId();
        assertThat(retrieved).isPresent().contains(companyId);
        assertThat(CompanyContextHolder.requireCompanyId()).isEqualTo(companyId);
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando requireCompanyId for chamado sem contexto")
    void requireCompanyId_ThrowsException_WhenContextEmpty() {
        assertThatThrownBy(CompanyContextHolder::requireCompanyId)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("X-Company-ID");
    }

    @Test
    @DisplayName("Deve limpar o contexto ao chamar clear()")
    void clear_RemovesContext() {
        CompanyContextHolder.setCompanyId(UUID.randomUUID());
        CompanyContextHolder.clear();

        assertThat(CompanyContextHolder.getCompanyId()).isEmpty();
    }
}