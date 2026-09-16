package com.shaluyadav.taskscheduler.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;


@Entity 
@Table(name="jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "cron_expression")
    private String cronExpression;

    @Column(name = "handler_type", nullable = false)
    private String handlerType;

    @Column(name = "handler_config", columnDefinition = "jsonb", nullable = false)
    private String handlerConfig;

    @Column (name = "max_retries", nullable = false)
    private int maxRetries=5;

    @Column (name = "timeout_seconds", nullable = false)
    private int timeoutSeconds = 300;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Job(){

    }

    // Getters and Setters
    public UUID getId(){
        return id;
    }

    public void setId(UUID id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getCronExpression(){
        return cronExpression;
    }

    public void setCronExpression(String cronExpression){
        this.cronExpression = cronExpression;
    }

    public String getHandlerType(){
        return handlerType;
    }

    public void setHandlerType(String handlerType){
        this.handlerType = handlerType;
    }

    public String getHandlerConfig(){
        return handlerConfig;
    }

    public void setHandlerConfigJson(String handlerConfig){
        this.handlerConfig = handlerConfig;
    }

    public int getMaxRetries(){
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries){
        this.maxRetries = maxRetries;
    }
    
    public int getTimeoutSeconds(){
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds){
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isEnabled(){
        return enabled;
    }
    
    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }
}

