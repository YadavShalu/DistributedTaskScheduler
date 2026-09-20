package com.shaluyadav.taskscheduler.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;


@Entity 
@Table(name="jobs")
@Getter 
@Setter
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

    @JdbcTypeCode(SqlTypes.JSON)
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

    
    
}

