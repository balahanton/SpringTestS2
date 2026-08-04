CREATE TABLE spring_test_s2.dead_letter_events
(
    id            UUID PRIMARY KEY,
    event_id      UUID,
    payload       TEXT        NOT NULL,
    error_message TEXT,
    received_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);