package com.futbolin.core.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        String code,
        String message,
        List<String> details,
        String path
) {
    public static ErrorResponse of(ErrorCode code, String message, String path) {
        return new ErrorResponse(Instant.now(), code.name(), message, List.of(), path);
    }
}
