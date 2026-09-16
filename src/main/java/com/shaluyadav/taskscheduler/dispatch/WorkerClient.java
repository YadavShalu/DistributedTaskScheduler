package com.shaluyadav.taskscheduler.dispatch;

import com.shaluyadav.taskscheduler.dto.JobDispatchRequest;

import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Component 
@Slf4j 
public class WorkerClient {
    private final RestTemplate restTemplate;
    
    public WorkerClient(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    public boolean send(String workerBaseUrl, JobDispatchRequest request){
        String executeUrl = workerBaseUrl + "/execute";
        try{
            var response = restTemplate.postForEntity(executeUrl, request, Void.class);
            boolean acknowledged = response.getStatusCode().is2xxSuccessful();
            if (!acknowledged) {
                log.warn("Worker at {} responded with non-2xx status {} for run {}",
                    workerBaseUrl, response.getStatusCode(), request.getRunId());
            }
            return acknowledged;
        }catch(ResourceAccessException e){
            log.warn("Failed to reach worker at {} for run {}: {}",
                workerBaseUrl, request.getRunId(), e.getMessage());
            return false;
        }catch (Exception e) {
            
            log.warn("Unexpected error dispatching run {} to worker at {}: {}",
                request.getRunId(), workerBaseUrl, e.getMessage());
            return false;
    }
    }
}
