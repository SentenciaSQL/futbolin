package com.futbolin.application.match;

import java.time.Instant;

public record InstantQueued(Instant value) {
    public static InstantQueued now() {
        return new InstantQueued(Instant.now());
    }
}
