package com.shaluyadav.taskscheduler.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "job_runs")
@Getter 
@Setter 
@NoArgsConstructor
public class JobRun {
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "job_id", nullable=false)
    private UUID jobId;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private RunStatus status;

    @Column(nullable=false)
    private int attempt = 0;

    @Column(name="worker_id")
    private String workerId;

    @Column(name="scheduler_for", nullable=false)
    private Instant scheduledFor;

    @Column(name= "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name="error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name="created_at", nullable=false)
    private Instant  createdAt = Instant.now();






}
