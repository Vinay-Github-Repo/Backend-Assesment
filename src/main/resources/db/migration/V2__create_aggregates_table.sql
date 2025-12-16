-- Aggregates table
CREATE TABLE aggregates (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    bucket_start TIMESTAMP WITH TIME ZONE NOT NULL,
    bucket_size VARCHAR(10) NOT NULL CHECK (bucket_size IN ('MINUTE', 'HOUR')),
    source VARCHAR(50),
    event_type VARCHAR(50),
    count BIGINT NOT NULL DEFAULT 0,
    first_seen TIMESTAMP WITH TIME ZONE,
    last_seen TIMESTAMP WITH TIME ZONE,
    last_aggregated_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_aggregate UNIQUE (tenant_id, bucket_start, bucket_size, source, event_type)
);

-- Indexes
CREATE INDEX idx_tenant_bucket ON aggregates(tenant_id, bucket_start DESC);
CREATE INDEX idx_bucket_start ON aggregates(bucket_start);

-- Comments
COMMENT ON TABLE aggregates IS 'Pre-computed event metrics aggregated by time buckets';
COMMENT ON COLUMN aggregates.bucket_start IS 'Start of the time bucket';
COMMENT ON COLUMN aggregates.bucket_size IS 'Size of time bucket: MINUTE or HOUR';
COMMENT ON COLUMN aggregates.version IS 'Optimistic locking version';