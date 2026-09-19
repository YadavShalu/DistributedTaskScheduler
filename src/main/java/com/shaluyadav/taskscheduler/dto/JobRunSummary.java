package com.shaluyadav.taskscheduler.dto;


import com.shaluyadav.taskscheduler.entity.RunStatus;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Builder
public class JobRunSummary {

    private UUID id;
    private UUID jobId;

    private String jobName;
    private RunStatus status;
    private int attempt;
    private String workerId;
    private Instant scheduledFor;
    private Instant startedAt;
    private Instant finishedAt;
    private String errorMessage;

    
}
