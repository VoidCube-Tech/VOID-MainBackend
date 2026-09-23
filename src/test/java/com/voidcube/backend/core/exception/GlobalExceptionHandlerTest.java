package com.voidcube.backend.core.exception;

import com.voidcube.backend.core.context.CorrelationContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        CorrelationContextHolder.setCorrelationId("test-corr-id-999");
    }

    @AfterEach
    void tearDown() {
        CorrelationContextHolder.clear();
    }

    @Test
    @DisplayName("Deve tratar BusinessException com status, código, correlation_id e timestamp")
    void handleBusinessException_ReturnsProblemDetail() {
        BusinessException ex = new BusinessException("PASSWORD_CHANGE_REQUIRED", "Troca necessária.", HttpStatus.FORBIDDEN);
        ProblemDetail pd = handler.handleBusinessException(ex);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(pd.getDetail()).isEqualTo("Troca necessária.");
        assertThat(pd.getProperties().get("code")).isEqualTo("PASSWORD_CHANGE_REQUIRED");
        assertThat(pd.getProperties().get("error_code")).isEqualTo("PASSWORD_CHANGE_REQUIRED");
        assertThat(pd.getProperties().get("correlation_id")).isEqualTo("test-corr-id-999");
        assertThat(pd.getProperties().get("timestamp")).isNotNull();
    }

    @Test
    @DisplayName("Deve tratar HttpMessageNotReadableException como 400 com código MALFORMED_JSON")
    void handleMessageNotReadable_Returns400() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON parse error", new MockHttpInputMessage(new byte[0]));
        ProblemDetail pd = handler.handleMessageNotReadable(ex);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(pd.getProperties().get("code")).isEqualTo("MALFORMED_JSON");
        assertThat(pd.getProperties().get("correlation_id")).isEqualTo("test-corr-id-999");
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentTypeMismatchException como 400 com código INVALID_PARAMETER")
    void handleTypeMismatch_Returns400() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "invalid-uuid",
                java.util.UUID.class,
                "eventId",
                null,
                null
        );

        ProblemDetail pd = handler.handleTypeMismatch(ex);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(pd.getProperties().get("code")).isEqualTo("INVALID_PARAMETER");
        assertThat(pd.getDetail()).contains("eventId");
    }

    @Test
    @DisplayName("Deve tratar HttpRequestMethodNotSupportedException como 405 com código METHOD_NOT_ALLOWED")
    void handleMethodNotSupported_Returns405() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("GET");
        ProblemDetail pd = handler.handleMethodNotSupported(ex);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());
        assertThat(pd.getProperties().get("code")).isEqualTo("METHOD_NOT_ALLOWED");
    }

    @Test
    @DisplayName("Deve tratar AccessDeniedException como 403 com código ACCESS_DENIED")
    void handleAccessDenied_Returns403() {
        AccessDeniedException ex = new AccessDeniedException("Sem permissão");
        ProblemDetail pd = handler.handleAccessDenied(ex);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(pd.getProperties().get("code")).isEqualTo("ACCESS_DENIED");
    }

    @Test
    @DisplayName("Deve tratar Exception genérica como 500 com código INTERNAL_SERVER_ERROR sem vazar detalhes")
    void handleGenericException_Returns500() {
        Exception ex = new RuntimeException("Falha catastrófica interna com stack trace");
        ProblemDetail pd = handler.handleGenericException(ex);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(pd.getProperties().get("code")).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(pd.getDetail()).isEqualTo("Ocorreu um erro interno inesperado no servidor.");
    }
}