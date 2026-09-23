package com.voidcube.backend.core.exception;

import com.voidcube.backend.core.context.CorrelationContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        problemDetail.setTitle("Regra de Negócio Violada");
        problemDetail.setType(URI.create("https://voidcube.com/errors/business-rule-violation"));
        enrich(problemDetail, ex.getCode());
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Dados de entrada inválidos");
        problemDetail.setTitle("Erro de Validação");
        problemDetail.setType(URI.create("https://voidcube.com/errors/invalid-input"));
        problemDetail.setProperty("invalid_fields", errors);
        enrich(problemDetail, "VALIDATION_FAILED");
        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMessageNotReadable(HttpMessageNotReadableException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "O corpo da requisição é inválido ou contém JSON malformado."
        );
        problemDetail.setTitle("Corpo da Requisição Inválido");
        problemDetail.setType(URI.create("https://voidcube.com/errors/malformed-json"));
        enrich(problemDetail, "MALFORMED_JSON");
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String detail = "O parâmetro '" + ex.getName() + "' recebeu um valor inválido: '" + ex.getValue() + "'.";
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problemDetail.setTitle("Parâmetro Inválido");
        problemDetail.setType(URI.create("https://voidcube.com/errors/invalid-parameter"));
        enrich(problemDetail, "INVALID_PARAMETER");
        return problemDetail;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String detail = "Método HTTP '" + ex.getMethod() + "' não é suportado para este endpoint.";
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, detail);
        problemDetail.setTitle("Método Não Permitido");
        problemDetail.setType(URI.create("https://voidcube.com/errors/method-not-allowed"));
        enrich(problemDetail, "METHOD_NOT_ALLOWED");
        return problemDetail;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "Acesso negado: você não possui permissão para executar esta operação."
        );
        problemDetail.setTitle("Acesso Negado");
        problemDetail.setType(URI.create("https://voidcube.com/errors/access-denied"));
        enrich(problemDetail, "ACCESS_DENIED");
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "Ocorreu um erro interno inesperado no servidor."
        );
        problemDetail.setTitle("Erro Interno do Servidor");
        problemDetail.setType(URI.create("https://voidcube.com/errors/internal-server-error"));
        enrich(problemDetail, "INTERNAL_SERVER_ERROR");
        return problemDetail;
    }

    private void enrich(ProblemDetail problemDetail, String code) {
        String correlationId = CorrelationContextHolder.getOrGenerate();
        problemDetail.setProperty("code", code);
        problemDetail.setProperty("error_code", code);
        problemDetail.setProperty("correlation_id", correlationId);
        problemDetail.setProperty("timestamp", Instant.now());
    }
}