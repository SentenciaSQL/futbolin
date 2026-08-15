package com.futbolin.domain.match;

import java.util.UUID;

public record SubmittedAnswer(
        UUID userId,
        String optionKey,
        boolean correct,
        int responseMs
) {}
