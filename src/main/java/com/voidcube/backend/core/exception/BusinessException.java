package com.voidcube.backend.core.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException  {
    
    private final String code;
    private final HttpStatus status;

    public BusinessException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public BusinessException(String message, HttpStatus status) {
        this("BUSINESS_RULE_VIOLATION", message, status);
    }

    public BusinessException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}