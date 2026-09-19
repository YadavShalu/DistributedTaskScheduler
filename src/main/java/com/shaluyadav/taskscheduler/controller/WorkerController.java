package com.shaluyadav.taskscheduler.controller;

import com.shaluyadav.taskscheduler.dispatch.WorkerRegistry;
import com.shaluyadav.taskscheduler.dto.WorkerRegistrationRequest;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController 
@RequestMapping("/workers")
public class WorkerController {
    private final WorkerRegistry workerRegistry;

    public WorkerController(WorkerRegistry workerRegistry){
        this.workerRegistry = workerRegistry;
    }

    @PostMapping("/register")
    public ResponseEntity<Void>registerOrHeartBeat(@Valid @RequestBody WorkerRegistrationRequest request){
        workerRegistry.registerOrHeartbeat(request.getWorkerId(),request.getBaseURL());

        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{workerId}")
    public ResponseEntity<Void> deregister(@PathVariable String workerId) {
        workerRegistry.deregister(workerId);
        return ResponseEntity.noContent().build();
    }

}
