package com.shaluyadav.taskscheduler.scheduler;

import java.util.UUID;

public record ScheduledEntry(UUID jobId, long fireTimeMillis) {
    
}
