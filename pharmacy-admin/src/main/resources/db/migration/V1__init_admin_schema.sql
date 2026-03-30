-- V1__init_admin_schema.sql

CREATE TABLE dashboard_cache (
    id BIGSERIAL PRIMARY KEY,
    metric_key VARCHAR(100) NOT NULL UNIQUE,
    metric_value DOUBLE PRECISION,
    metric_data TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE report_snapshots (
    id BIGSERIAL PRIMARY KEY,
    report_type VARCHAR(100) NOT NULL,
    report_date DATE NOT NULL,
    report_data TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dashboard_cache_key ON dashboard_cache(metric_key);
CREATE INDEX idx_report_snapshots_type_date ON report_snapshots(report_type, report_date);
