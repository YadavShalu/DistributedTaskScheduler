CREATE TABLE jobs(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    cron expression VARCHAR(100), -- nullable : DAG-triggered job may have no cron
    handler_type  VARCHAR(100) NOT NULL,
    handler_config JSONB NOT NULL,
    max_retries INT NOT NULL DEFAULT 5,
    timeout_seconds INT NOT NULL DEFAULT 300,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
);

CREATE TABLE job_dependecies(
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    depends_on_job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    PRIMARY_KEY (job_id, depends_on_job_id)

);

CREATE TABLE job_runs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL REFERENCES jobs(id),
    status VARCHAR(20) NOT NULL,           -- PENDING, DISPATCHED, RUNNING, SUCCEEDED, FAILED, DEAD_LETTERED
    attempt INT NOT NULL DEFAULT 0,
    worker_id VARCHAR(100),
    scheduled_for TIMESTAMPTZ NOT NULL,
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_job_runs_job_id ON job_runs(job_id);
CREATE INDEX idx_job_runs_status ON job_runs(status);
CREATE INDEX idx_job_dependencies_job_id ON job_dependencies(job_id);