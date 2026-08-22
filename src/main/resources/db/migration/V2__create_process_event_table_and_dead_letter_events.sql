CREATE TABLE spring_test_s2.processed_events
(
    event_id     UUID PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE spring_test_s2.dead_letter_events
(
    id            UUID PRIMARY KEY,
    event_id      UUID,
    message_key   TEXT        NOT NULL UNIQUE,
    payload       TEXT        NOT NULL,
    error_message TEXT,
    received_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE spring_test_s2.delivery_enrichments
(
    id          UUID PRIMARY KEY,
    delivery_id UUID        NOT NULL,
    address     TEXT        NOT NULL,
    status      TEXT        NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_delivery_enrichments_delivery_id
    ON spring_test_s2.delivery_enrichments (delivery_id);