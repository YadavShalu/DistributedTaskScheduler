CREATE TABLE jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    cron_expression VARCHAR(100),               --nullable: DAG-triggered jobs
    handler_type VARCHAR(100) NOT NULL,         --e.g. "HTTP_CALL", "SHELL_COMMMAND",
    handler_config JSONB NOT NULL,                --e.g. {"url": "http://example.com", "method": "POST"}    
    max_retries INT NOT NULL DEFAULT 5,
    timeout_seconds INT NOT NULL DEFAULT 300,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEAFULT now()
);

CREATE TABLE job_dependencies (
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    depends_on_job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    PRIMARY KEY (job_id, depends_on_job_id)
);

CREATE TABLE job_runs (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   job_id UUID NOT NULL REFERENCES jobs(id),
   status VARCHAR(20) NOT NULL          -- PENDING, RUNNING, DISPATCHED, SUCCEEDED, FAILED, DEAD_LETTERED
   attempt INT NOT NULL DEFAULT 0,
   worker_id VARCHAR(100),
   scheduled_for TIMESTAMPTZ NOT NULL,
   STARTED_AT TIMESTAMPTZ,
   finished_at TIMESTAMPTZ,,
   error_message TEXT,
   created_at TIMESTAMP NOT NULL DEFAULT now() 
);

CREATE INDEX idx_job_runs_job_id ON job_runs(job_id);
CREATE INDEX idx_job_runs_status ON job_runs(status);
CREATE INDEX idx_job_dependencies_job_id ON job_dependencies(job_id);