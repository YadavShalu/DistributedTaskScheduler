package com.shaluyadav.taskscheduler.controller;

import com.shaluyadav.taskscheduler.dto.RunCompletionRequest;
import com.shaluyadav.taskscheduler.service.RetryService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.UUID;

@RestController
@RequestMapping("/runs")
public class RunController {
    private final RetryService retryService;

    public RunController(RetryService retryService){
        this.retryService = retryService;
    }

    @PostMapping("/{runId}/complete")
    public ResponseEntity<Void> completeRun(@PathVariable UUID runId,
                                            @Valid @RequestBody RunCompletionRequest request)
                                            {
                                                retryService.handleCompletion(runId, request.isSuccess(), request.getErrorMessage());
                                                return ResponseEntity.noContent().build();
                                            }
}
