CREATE TABLE spring_test_s2.processed_events
(
    event_id     UUID PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);