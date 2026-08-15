package com.futbolin.core.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private final ErrorCode code;
    private final HttpStatus status;

    public ApiException(ErrorCode code) {
        this(code, code.defaultMessage());
    }

    public ApiException(ErrorCode code, String message) {
        super(message);
        this.code = code;
        this.status = code.status();
    }

    public ErrorCode code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }
}
