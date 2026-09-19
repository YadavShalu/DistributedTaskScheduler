package com.shaluyadav.taskscheduler.dto;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
public class RunCompletionRequest {
    private boolean success;
    private String errorMessage;
    
}
