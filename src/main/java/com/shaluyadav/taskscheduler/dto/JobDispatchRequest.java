package com.shaluyadav.taskscheduler.dto;

import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter 
@AllArgsConstructor 
public class JobDispatchRequest {
    private UUID runId;
    private UUID jobId;
    private String handlerType;
    private Map<String, Object>handlerConfig;
    private int timeoutSeconds;

}
