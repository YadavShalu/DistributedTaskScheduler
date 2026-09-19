package com.shaluyadav.taskscheduler.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
public class WorkerRegistrationRequest {

    @NotBlank(message = "workerId is required")
    private String workerId;

    @NotBlank(message = "baseUrl is required")
    private String baseURL;
    
}
