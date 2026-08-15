-- Device tokens, tournaments and weekly helpers

CREATE TABLE device_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    token VARCHAR(512) NOT NULL,
    platform VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_seen_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_device_token UNIQUE (token)
);

CREATE INDEX idx_device_tokens_user ON device_tokens (user_id);

CREATE TABLE tournaments (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(80) NOT NULL,
    theme VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    size INTEGER NOT NULL DEFAULT 16,
    starts_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_tournament_slug UNIQUE (slug)
);

CREATE TABLE tournament_entries (
    id UUID PRIMARY KEY,
    tournament_id UUID NOT NULL REFERENCES tournaments (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id),
    seed INTEGER,
    eliminated BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_tournament_user UNIQUE (tournament_id, user_id)
);

CREATE INDEX idx_tournament_entries_t ON tournament_entries (tournament_id);

CREATE TABLE tournament_matches (
    id UUID PRIMARY KEY,
    tournament_id UUID NOT NULL REFERENCES tournaments (id) ON DELETE CASCADE,
    round_name VARCHAR(20) NOT NULL,
    slot INTEGER NOT NULL,
    player_a_id UUID REFERENCES users (id),
    player_b_id UUID REFERENCES users (id),
    winner_id UUID REFERENCES users (id),
    match_id UUID REFERENCES matches (id),
    status VARCHAR(20) NOT NULL,
    CONSTRAINT uk_tournament_slot UNIQUE (tournament_id, round_name, slot)
);

CREATE INDEX idx_tournament_matches_match ON tournament_matches (match_id);
