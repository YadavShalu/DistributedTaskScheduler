package com.shaluyadav.taskscheduler.dto;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JobSummary {
    private UUID id;
    private String name;
    private String cronExpression;
    private String handlerType;
    private boolean enabled;

    private String lastRunStatus;
    private Instant lastRunAt;

    private ZonedDateTime nextFireTime;
}
