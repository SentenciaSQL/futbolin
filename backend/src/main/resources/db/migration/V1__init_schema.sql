-- Futbolin core schema (PostgreSQL / H2 PostgreSQL mode compatible)

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    username VARCHAR(32) NOT NULL,
    password_hash VARCHAR(255),
    provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    provider_id VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_username UNIQUE (username)
);

CREATE TABLE user_profiles (
    user_id UUID PRIMARY KEY REFERENCES users (id),
    display_name VARCHAR(64) NOT NULL,
    avatar_key VARCHAR(64) NOT NULL DEFAULT 'default',
    frame_key VARCHAR(64) NOT NULL DEFAULT 'default',
    title_key VARCHAR(64),
    country VARCHAR(8),
    favorite_team VARCHAR(80),
    level INTEGER NOT NULL DEFAULT 1,
    xp BIGINT NOT NULL DEFAULT 0,
    coins INTEGER NOT NULL DEFAULT 100,
    matches_played INTEGER NOT NULL DEFAULT 0,
    wins INTEGER NOT NULL DEFAULT 0,
    losses INTEGER NOT NULL DEFAULT 0,
    draws INTEGER NOT NULL DEFAULT 0,
    goals_scored INTEGER NOT NULL DEFAULT 0,
    goals_conceded INTEGER NOT NULL DEFAULT 0,
    correct_answers INTEGER NOT NULL DEFAULT 0,
    total_answers INTEGER NOT NULL DEFAULT 0,
    best_answer_streak INTEGER NOT NULL DEFAULT 0,
    current_answer_streak INTEGER NOT NULL DEFAULT 0,
    daily_streak INTEGER NOT NULL DEFAULT 0,
    last_daily_claim DATE,
    ranking_points INTEGER NOT NULL DEFAULT 1000,
    peak_ranking_points INTEGER NOT NULL DEFAULT 1000,
    division VARCHAR(20) NOT NULL DEFAULT 'AMATEUR',
    average_answer_ms INTEGER,
    survival_best INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash)
);

CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE question_categories (
    id UUID PRIMARY KEY,
    code VARCHAR(40) NOT NULL,
    name_es VARCHAR(80) NOT NULL,
    name_en VARCHAR(80) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_category_code UNIQUE (code)
);

CREATE TABLE questions (
    id UUID PRIMARY KEY,
    category_id UUID NOT NULL REFERENCES question_categories (id),
    type VARCHAR(30) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    prompt_es VARCHAR(1000) NOT NULL,
    prompt_en VARCHAR(1000) NOT NULL,
    explanation_es VARCHAR(1000),
    explanation_en VARCHAR(1000),
    image_url VARCHAR(500),
    metadata_json TEXT,
    correct_key VARCHAR(32),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    times_asked INTEGER NOT NULL DEFAULT 0,
    times_correct INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_questions_category ON questions (category_id);
CREATE INDEX idx_questions_active_diff ON questions (active, difficulty);
CREATE INDEX idx_questions_prompt_es ON questions (prompt_es);

CREATE TABLE question_options (
    id UUID PRIMARY KEY,
    question_id UUID NOT NULL REFERENCES questions (id) ON DELETE CASCADE,
    option_key VARCHAR(16) NOT NULL,
    text_es VARCHAR(500) NOT NULL,
    text_en VARCHAR(500) NOT NULL,
    correct BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_options_question ON question_options (question_id);

CREATE TABLE question_reports (
    id UUID PRIMARY KEY,
    question_id UUID NOT NULL REFERENCES questions (id),
    reporter_id UUID NOT NULL REFERENCES users (id),
    reason VARCHAR(40) NOT NULL,
    details VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    admin_note VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    resolved_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE ranking_seasons (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(80) NOT NULL,
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_season_slug UNIQUE (slug)
);

CREATE TABLE rankings (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    season_id UUID NOT NULL REFERENCES ranking_seasons (id),
    points INTEGER NOT NULL DEFAULT 1000,
    division VARCHAR(20) NOT NULL DEFAULT 'AMATEUR',
    matches_played INTEGER NOT NULL DEFAULT 0,
    wins INTEGER NOT NULL DEFAULT 0,
    losses INTEGER NOT NULL DEFAULT 0,
    draws INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_ranking_user_season UNIQUE (user_id, season_id)
);

CREATE TABLE matches (
    id UUID PRIMARY KEY,
    mode VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    private_code VARCHAR(16),
    season_id UUID REFERENCES ranking_seasons (id),
    player_a_id UUID REFERENCES users (id),
    player_b_id UUID REFERENCES users (id),
    score_a INTEGER NOT NULL DEFAULT 0,
    score_b INTEGER NOT NULL DEFAULT 0,
    winner_id UUID REFERENCES users (id),
    end_reason VARCHAR(30),
    duration_seconds INTEGER NOT NULL DEFAULT 240,
    goals_to_win INTEGER NOT NULL DEFAULT 3,
    ball_position INTEGER NOT NULL DEFAULT 0,
    possession_user_id UUID,
    pitch_phase VARCHAR(20) NOT NULL DEFAULT 'KICKOFF',
    started_at TIMESTAMP WITH TIME ZONE,
    ended_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_matches_private_code UNIQUE (private_code)
);

CREATE INDEX idx_matches_players ON matches (player_a_id, player_b_id);
CREATE INDEX idx_matches_status ON matches (status);
CREATE INDEX idx_matches_created ON matches (created_at);

CREATE TABLE match_players (
    id UUID PRIMARY KEY,
    match_id UUID NOT NULL REFERENCES matches (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id),
    slot VARCHAR(1) NOT NULL,
    rating_before INTEGER,
    rating_after INTEGER,
    rating_delta INTEGER,
    correct_answers INTEGER NOT NULL DEFAULT 0,
    wrong_answers INTEGER NOT NULL DEFAULT 0,
    goals INTEGER NOT NULL DEFAULT 0,
    average_answer_ms INTEGER,
    xp_earned INTEGER NOT NULL DEFAULT 0,
    coins_earned INTEGER NOT NULL DEFAULT 0,
    muted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_match_player UNIQUE (match_id, user_id)
);

CREATE TABLE match_rounds (
    id UUID PRIMARY KEY,
    match_id UUID NOT NULL REFERENCES matches (id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions (id),
    round_number INTEGER NOT NULL,
    phase VARCHAR(20) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    closed_at TIMESTAMP WITH TIME ZONE,
    winner_user_id UUID,
    shuffled_keys TEXT
);

CREATE INDEX idx_rounds_match ON match_rounds (match_id, round_number);

CREATE TABLE match_answers (
    id UUID PRIMARY KEY,
    round_id UUID NOT NULL REFERENCES match_rounds (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id),
    option_key VARCHAR(16),
    correct BOOLEAN NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    response_ms INTEGER NOT NULL,
    CONSTRAINT uk_round_user UNIQUE (round_id, user_id)
);

CREATE TABLE match_events (
    id UUID PRIMARY KEY,
    match_id UUID NOT NULL REFERENCES matches (id) ON DELETE CASCADE,
    event_type VARCHAR(40) NOT NULL,
    payload_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_match_events_match ON match_events (match_id, created_at);

CREATE TABLE match_invitations (
    id UUID PRIMARY KEY,
    match_id UUID NOT NULL REFERENCES matches (id),
    host_id UUID NOT NULL REFERENCES users (id),
    guest_id UUID REFERENCES users (id),
    code VARCHAR(16) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE rivalries (
    id UUID PRIMARY KEY,
    user_a_id UUID NOT NULL REFERENCES users (id),
    user_b_id UUID NOT NULL REFERENCES users (id),
    matches_played INTEGER NOT NULL DEFAULT 0,
    wins_a INTEGER NOT NULL DEFAULT 0,
    wins_b INTEGER NOT NULL DEFAULT 0,
    draws INTEGER NOT NULL DEFAULT 0,
    last_match_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_rivalry UNIQUE (user_a_id, user_b_id)
);

CREATE TABLE achievements (
    id UUID PRIMARY KEY,
    code VARCHAR(40) NOT NULL,
    name_es VARCHAR(80) NOT NULL,
    name_en VARCHAR(80) NOT NULL,
    description_es VARCHAR(255) NOT NULL,
    description_en VARCHAR(255) NOT NULL,
    xp_reward INTEGER NOT NULL DEFAULT 50,
    coins_reward INTEGER NOT NULL DEFAULT 25,
    CONSTRAINT uk_achievement_code UNIQUE (code)
);

CREATE TABLE user_achievements (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    achievement_id UUID NOT NULL REFERENCES achievements (id),
    unlocked_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_user_achievement UNIQUE (user_id, achievement_id)
);

CREATE TABLE missions (
    id UUID PRIMARY KEY,
    code VARCHAR(40) NOT NULL,
    period VARCHAR(20) NOT NULL,
    name_es VARCHAR(80) NOT NULL,
    name_en VARCHAR(80) NOT NULL,
    description_es VARCHAR(255) NOT NULL,
    description_en VARCHAR(255) NOT NULL,
    metric VARCHAR(40) NOT NULL,
    target INTEGER NOT NULL,
    xp_reward INTEGER NOT NULL,
    coins_reward INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_mission_code UNIQUE (code)
);

CREATE TABLE user_missions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    mission_id UUID NOT NULL REFERENCES missions (id),
    period_key VARCHAR(20) NOT NULL,
    progress INTEGER NOT NULL DEFAULT 0,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    claimed BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_user_mission_period UNIQUE (user_id, mission_id, period_key)
);

CREATE TABLE cosmetics (
    id UUID PRIMARY KEY,
    cosmetic_key VARCHAR(64) NOT NULL,
    type VARCHAR(30) NOT NULL,
    name_es VARCHAR(80) NOT NULL,
    name_en VARCHAR(80) NOT NULL,
    rarity VARCHAR(20) NOT NULL DEFAULT 'COMMON',
    price_coins INTEGER NOT NULL DEFAULT 0,
    min_level INTEGER NOT NULL DEFAULT 1,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_cosmetic_key UNIQUE (cosmetic_key)
);

CREATE TABLE user_cosmetics (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    cosmetic_id UUID NOT NULL REFERENCES cosmetics (id),
    equipped BOOLEAN NOT NULL DEFAULT FALSE,
    unlocked_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_user_cosmetic UNIQUE (user_id, cosmetic_id)
);

CREATE TABLE daily_login_rewards (
    id UUID PRIMARY KEY,
    day_index INTEGER NOT NULL,
    coins INTEGER NOT NULL DEFAULT 0,
    xp INTEGER NOT NULL DEFAULT 0,
    cosmetic_key VARCHAR(64),
    CONSTRAINT uk_daily_day UNIQUE (day_index)
);

CREATE TABLE friendships (
    id UUID PRIMARY KEY,
    requester_id UUID NOT NULL REFERENCES users (id),
    addressee_id UUID NOT NULL REFERENCES users (id),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_friendship UNIQUE (requester_id, addressee_id)
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    type VARCHAR(40) NOT NULL,
    title_es VARCHAR(120) NOT NULL,
    title_en VARCHAR(120) NOT NULL,
    body_es VARCHAR(255) NOT NULL,
    body_en VARCHAR(255) NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE daily_challenges (
    id UUID PRIMARY KEY,
    challenge_date DATE NOT NULL,
    question_id UUID NOT NULL REFERENCES questions (id),
    total_answers INTEGER NOT NULL DEFAULT 0,
    correct_answers INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uk_daily_date UNIQUE (challenge_date)
);

CREATE TABLE daily_challenge_answers (
    id UUID PRIMARY KEY,
    challenge_id UUID NOT NULL REFERENCES daily_challenges (id),
    user_id UUID NOT NULL REFERENCES users (id),
    option_key VARCHAR(16),
    correct BOOLEAN NOT NULL,
    answered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_daily_user UNIQUE (challenge_id, user_id)
);

CREATE TABLE survival_runs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    score INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE player_category_stats (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    category_id UUID NOT NULL REFERENCES question_categories (id),
    correct INTEGER NOT NULL DEFAULT 0,
    total INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uk_user_category UNIQUE (user_id, category_id)
);

CREATE INDEX idx_rankings_season_points ON rankings (season_id, points DESC);
CREATE INDEX idx_profiles_ranking ON user_profiles (ranking_points DESC);
CREATE INDEX idx_notifications_user ON notifications (user_id, read);
