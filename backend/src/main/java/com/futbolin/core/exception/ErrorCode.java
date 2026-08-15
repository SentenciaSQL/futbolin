package com.futbolin.core.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION(HttpStatus.BAD_REQUEST, "Invalid request"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    CONFLICT(HttpStatus.CONFLICT, "Conflict"),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "Too many requests"),
    EMAIL_TAKEN(HttpStatus.CONFLICT, "Email already registered"),
    USERNAME_TAKEN(HttpStatus.CONFLICT, "Username already taken"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid credentials"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid or expired token"),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "Account is locked"),
    MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "Match not found"),
    MATCH_FULL(HttpStatus.CONFLICT, "Match is full"),
    MATCH_ALREADY_STARTED(HttpStatus.CONFLICT, "Match already started"),
    NOT_YOUR_TURN(HttpStatus.CONFLICT, "Action not allowed in current state"),
    ANSWER_TOO_FAST(HttpStatus.BAD_REQUEST, "Impossible answer speed"),
    ALREADY_ANSWERED(HttpStatus.CONFLICT, "Already answered this round"),
    QUESTION_REPORTED(HttpStatus.CONFLICT, "Question already reported"),
    INSUFFICIENT_COINS(HttpStatus.CONFLICT, "Not enough Football Coins"),
    IMPORT_FAILED(HttpStatus.BAD_REQUEST, "Question import failed"),
    TOURNAMENT_FULL(HttpStatus.CONFLICT, "Tournament is full"),
    TOURNAMENT_NOT_JOINABLE(HttpStatus.CONFLICT, "Tournament is not open for registration"),
    ALREADY_JOINED(HttpStatus.CONFLICT, "Already joined"),
    NOT_YOUR_MATCH(HttpStatus.FORBIDDEN, "Not your match");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus status() {
        return status;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
