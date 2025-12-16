-- Idempotency keys table
CREATE TABLE idempotency_keys (
    idempotency_key VARCHAR(200) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    event_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE
);

-- Indexes
CREATE INDEX idx_tenant_event ON idempotency_keys(tenant_id, event_id);
CREATE INDEX idx_created_at ON idempotency_keys(created_at);
CREATE INDEX idx_status ON idempotency_keys(status);

-- Comments
COMMENT ON TABLE idempotency_keys IS 'Tracks processed events to ensure idempotent operations';
COMMENT ON COLUMN idempotency_keys.idempotency_key IS 'Composite key: tenant_id:event_id';
COMMENT ON COLUMN idempotency_keys.status IS 'Processing status of the event';

-- Cleanup old keys (optional: run as scheduled job)
-- DELETE FROM idempotency_keys WHERE created_at < NOW() - INTERVAL '7 days' AND status = 'COMPLETED';