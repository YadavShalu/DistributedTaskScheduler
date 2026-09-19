package com.shaluyadav.taskscheduler.controller;

import com.shaluyadav.taskscheduler.dto.CreateJobRequest;
import com.shaluyadav.taskscheduler.service.JobService;
import com.shaluyadav.taskscheduler.entity.Job;

import com.shaluyadav.taskscheduler.dto.JobResponse;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController 
@RequestMapping("/jobs")
public class JobController {
    private final JobService jobService;
    
    public JobController(JobService jobService){
        this.jobService = jobService;
    }

    @PostMapping 
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody CreateJobRequest request){
        Job createdJob = jobService.createJob(request);

        JobResponse responseBody = JobResponse.from(createdJob);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);

    }

    @GetMapping("/id")
    public ResponseEntity<JobResponse> getJob(@PathVariable UUID id){
        Job job = jobService.getJob(id);
        return ResponseEntity.ok(JobResponse.from(job));
    }
    
}
