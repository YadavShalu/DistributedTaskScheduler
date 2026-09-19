package com.shaluyadav.taskscheduler.dto;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.shaluyadav.taskscheduler.entity.Job;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter 
@Builder 
public class JobResponse {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private UUID id;
    private String name;
    private String cronExpression;
    private String handlerType; 
    private Map<String,Object> handlerConfig;   
    private int maxRetries;
    private int timeoutSeconds;
    private boolean enabled;
    private Instant createdAt;


    public static JobResponse from(Job job){
        Map<String,Object> parsedConfig;
        
        try{
            parsedConfig = MAPPER.readValue(
                job.getHandlerConfig(),
            new com.fasterxml.jackson.core.type.TypeReference<Map<String,Object>>(){});
        }catch(JsonProcessingException e){
            throw new IllegalStateException(
                "Corrupted handlerConfigJosn for job "+ job.getId(), e
            );
        }
        return JobResponse.builder()
            .id(job.getId())
            .name(job.getName())
            .cronExpression(job.getCronExpression())
            .handlerType(job.getHandlerType())
            .handlerConfig(parsedConfig)
            .maxRetries(job.getMaxRetries())
            .timeoutSeconds(job.getTimeoutSeconds())
            .enabled(job.isEnabled())
            .build();
    }
}
