CREATE TABLE IF NOT EXISTS user_interests (
                                              id BIGSERIAL PRIMARY KEY,
                                              keyword VARCHAR(255) NOT NULL,
    mentioned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_checked_at TIMESTAMPTZ,
    active BOOLEAN NOT NULL DEFAULT true
    );

CREATE TABLE IF NOT EXISTS activity_logs (
                                             id BIGSERIAL PRIMARY KEY,
                                             content TEXT NOT NULL,
                                             created_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );