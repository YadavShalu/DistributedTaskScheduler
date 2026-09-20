package com.shaluyadav.taskscheduler.service;

import com.shaluyadav.taskscheduler.algorithm.TopologicalSort;
import com.shaluyadav.taskscheduler.entity.Job;
import com.shaluyadav.taskscheduler.entity.JobDependency;
import com.shaluyadav.taskscheduler.repository.JobDependencyRepository;
import com.shaluyadav.taskscheduler.repository.JobRepository;
import com.shaluyadav.taskscheduler.scheduler.ScheduleJobEvent;
import com.shaluyadav.taskscheduler.dto.CreateJobRequest;

import org.springframework.stereotype.Service;

import org.springframework.context.ApplicationEventPublisher;

import jakarta.transaction.Transactional;
import tools.jackson.databind.ObjectMapper;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobDependencyRepository dependencyRepository;
    private final CronService cronService;
    private final TopologicalSort topologicalSort;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApplicationEventPublisher eventPublisher;

    public JobService(JobRepository jobRepository, 
        JobDependencyRepository dependencyRepository,
        CronService cronService,
        TopologicalSort topologicalSort,
        ApplicationEventPublisher applicationEventPublisher) {
            this.jobRepository = jobRepository;
            this.dependencyRepository = dependencyRepository;
            this.cronService = cronService;
            this.topologicalSort = topologicalSort;
            this.eventPublisher = applicationEventPublisher;

        }

    @Transactional 
    public Job createJob(CreateJobRequest request){
        boolean hasCron = request.getCronExpression() != null && !request.getCronExpression().isEmpty();
        boolean hasDependencies = request.getDependsOn().isEmpty();
        if(!hasCron && !hasDependencies){
            throw new IllegalArgumentException("Job must have either a cron expression or dependencies");
        }

        if(hasCron){
            cronService.validate(request.getCronExpression());
        }

        if(hasDependencies){
            validateNoCycleIntroduced(request.getDependsOn());
        }

        Job job = new Job();
        job.setName(request.getName());
        job.setCronExpression(request.getCronExpression());
        job.setHandlerType(request.getHandlerType());
        job.setHandlerConfig(serializeHandlerConfig(request.getHandlerConfig()));
        job.setMaxRetries(request.getMaxRetries());
        job.setTimeoutSeconds(request.getTimeoutSeconds());
        job.setEnabled(true);

        Job savedJob = jobRepository.save(job);

        if(savedJob.getCronExpression() != null){
            eventPublisher.publishEvent(new ScheduleJobEvent(savedJob.getId()));
        }

        for(UUID parentId: request.getDependsOn()){
            JobDependency dependency = new JobDependency(savedJob.getId(), parentId);
            dependencyRepository.save(dependency); {
            };
        }
        return savedJob;
    }

    

    public Job getJob(UUID id){
        return jobRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + id));
    }

    public ZonedDateTime computeNextFireTime(Job job){
        if(job.getCronExpression() == null){
            return null;
        }
        return cronService.nextFireTime(job.getCronExpression(), ZonedDateTime.now());
    }

    private void validateNoCycleIntroduced(List<UUID> newDependsOn){
        Map<String,List<String>> adjacency = new HashMap<>();

        for(JobDependency edge : dependencyRepository.findAllEdges()){
            String from = edge.getDependsOnJobId().toString();
            String to = edge.getJobId().toString();
            adjacency.computeIfAbsent(from, k->new ArrayList<>()).add(to);
        }

        String newJobPlaceholder = "NEW_JOB_BEING_CREATED";
        for(UUID parentId: newDependsOn){
            adjacency.computeIfAbsent(parentId.toString(),k->new ArrayList<>())
                            .add(newJobPlaceholder);
        }

        topologicalSort.sort(adjacency);
    }

    private String serializeHandlerConfig(Map<String, Object>handlerConfig){
        try{
            return objectMapper.writeValueAsString(handlerConfig);
        } catch(Exception e){
            throw new RuntimeException("Failed to serialize handler config", e);
        }
    }
}
