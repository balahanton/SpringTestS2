CREATE TABLE user_enrichments
(
    id                    UUID PRIMARY KEY DEFAULT spring_test.uuid_generate_v4(),
    user_id               UUID           NOT NULL,
    discount_card_number  TEXT           NOT NULL,
    balance               NUMERIC(19, 2) NOT NULL DEFAULT 0,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_deleted            BOOLEAN        NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX uq_user_enrichments_user_id ON spring_test_s2.user_enrichments (user_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_user_enrichments_discount_card_number ON spring_test_s2.user_enrichments (discount_card_number);