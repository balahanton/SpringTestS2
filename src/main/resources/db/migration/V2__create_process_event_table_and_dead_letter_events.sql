CREATE TABLE spring_test_s2.processed_events
(
    event_id     UUID PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE spring_test_s2.dead_letter_events
(
    id            UUID PRIMARY KEY,
    event_id      UUID UNIQUE,
    payload       TEXT        NOT NULL,
    error_message TEXT,
    received_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);