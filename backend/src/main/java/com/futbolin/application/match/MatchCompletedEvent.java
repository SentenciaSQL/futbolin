package com.futbolin.application.match;

import com.futbolin.domain.match.MatchMode;

import java.util.UUID;

public record MatchCompletedEvent(UUID matchId, UUID winnerId, MatchMode mode) {}
