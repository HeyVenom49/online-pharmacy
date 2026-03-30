-- V2__add_processed_events.sql

CREATE TABLE processed_events (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(100) NOT NULL,
    consumer VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(event_id)
);

CREATE INDEX idx_consumer_event ON processed_events(consumer, event_id);

-- V3__add_inventory_version.sql

ALTER TABLE inventory ADD COLUMN version BIGINT DEFAULT 0;
