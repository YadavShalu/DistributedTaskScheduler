package com.shaluyadav.taskscheduler.dispatch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.shaluyadav.taskscheduler.dto.JobDispatchRequest;
import com.shaluyadav.taskscheduler.entity.Job;
import com.shaluyadav.taskscheduler.entity.JobRun;
import com.shaluyadav.taskscheduler.entity.RunStatus;
import com.shaluyadav.taskscheduler.repository.JobRunRepository;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Component 
@Slf4j 
public class JobDispatcher {
    private final WorkerRegistry workerRegistry;
    private final WorkerClient workerClient;
    private final JobRunRepository jobRunRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JobDispatcher(WorkerRegistry workerRegistry,
                            WorkerClient workerClient,
                            JobRunRepository jobRunRepository){
        this.workerClient = workerClient;
        this.workerRegistry = workerRegistry;
        this.jobRunRepository = jobRunRepository;
    }

    public JobRun dispatch(Job job, int attempt){
        JobRun run = new JobRun();
        run.setJobId(job.getId());
        run.setAttempt(attempt);
        run.setStatus((RunStatus.PENDING));
        run.setScheduledFor(Instant.now());

        run = jobRunRepository.save(run);

        String workerUrl;

        try{
            workerUrl = workerRegistry.getWorkerUrlFor(job.getId().toString());
        }catch(IllegalStateException e){
            log.warn("No worker available to dispatch job {} (run {}) ", job.getId(), run.getId());
            return run;
        }

        JobDispatchRequest dispatchRequest = new JobDispatchRequest(
            run.getId(),
            job.getId(),
            job.getHandlerType(),
            parseHandlerConfig(job.getHandlerConfig()),
            job.getTimeoutSeconds()
        );

        boolean acknowledged = workerClient.send(workerUrl, dispatchRequest);
        if(acknowledged){
            run.setStatus(RunStatus.DISPATCHED);
            run.setWorkerId(extractWorkerIdFromUrl(workerUrl));
            run.setStartedAt(Instant.now());
            log.info("Dispatched job {} (run {}) to worker at {}", job.getId(), run.getId(), workerUrl);
        }
        else{
            run.setStatus(RunStatus.FAILED);
            run.setErrorMessage("Failed to dispatch to worker at "+ workerUrl);
            log.warn("Dispatch attempt failed for job {} (run {}) to worker at {}",
                job.getId(), run.getId(), workerUrl);
        }

        return jobRunRepository.save(run);
    }
    
    private Map<String, Object> parseHandlerConfig(String handlerConfigJson){
        try{
            return objectMapper.readValue(handlerConfigJson, new TypeReference<Map<String,Object>>() {});
        }catch(Exception e){
            throw new IllegalStateException("CorruptedhandlerConfigJson", e);
        }
    }

    private String extractWorkerIdFromUrl(String workerUrl){
        String withoutProtocol = workerUrl.replaceFirst("^https?://", "");
        int colonIndex = withoutProtocol.indexOf(":");
        return colonIndex >0 ? withoutProtocol.substring(0,colonIndex):withoutProtocol;

    }
}
