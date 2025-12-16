-- Events table
CREATE TABLE events (
    event_id VARCHAR(100) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    source VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    payload JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    kafka_topic VARCHAR(100),
    kafka_partition INTEGER,
    kafka_offset BIGINT,
    processing_attempts INTEGER NOT NULL DEFAULT 0
);

-- Indexes for performance
CREATE INDEX idx_tenant_timestamp ON events(tenant_id, timestamp DESC);
CREATE INDEX idx_tenant_source_type ON events(tenant_id, source, event_type);
CREATE INDEX idx_created_at ON events(created_at);
CREATE INDEX idx_kafka_offset ON events(kafka_partition, kafka_offset);
CREATE INDEX idx_timestamp ON events(timestamp);

-- Comments
COMMENT ON TABLE events IS 'Raw event data from multiple sources';
COMMENT ON COLUMN events.event_id IS 'Globally unique identifier for the event';
COMMENT ON COLUMN events.tenant_id IS 'Tenant/customer identifier';
COMMENT ON COLUMN events.timestamp IS 'Event occurrence time in UTC';
COMMENT ON COLUMN events.payload IS 'Arbitrary event data in JSON format';