package com.shaluyadav.taskscheduler.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter 
@Setter 
public class CreateJobRequest {

    @NotBlank(message = "name is required")
    private String name;

    private String cronExpression;

    @NotBlank(message = "handlerType is required")
    private String handlerType;

    @NotNull(message = "handlerConfig is required")
    private Map<String, Object> handlerConfig;

    @Positive(message = "maxRetries must be positive")
    private int maxRetries=5;

    @Positive(message = "timeoutSeconds must be positive")
    private int timeoutSeconds = 300;

    private List<UUID> dependsOn = List.of();
    
}
