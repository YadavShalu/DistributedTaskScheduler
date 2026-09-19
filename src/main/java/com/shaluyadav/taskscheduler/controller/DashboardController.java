package com.shaluyadav.taskscheduler.controller;


import com.shaluyadav.taskscheduler.dto.JobRunSummary;
import com.shaluyadav.taskscheduler.dto.JobSummary;
import com.shaluyadav.taskscheduler.service.DashboardService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController 
@RequestMapping("/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService){
        this.dashboardService = dashboardService;
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<JobSummary>> listJobs(){
        return ResponseEntity.ok(dashboardService.listJobs());
    }

    @GetMapping("/jobs/{id}/runs")
    public ResponseEntity<List<JobRunSummary>> getRunHistory(
        @PathVariable UUID id,
        @RequestParam(defaultValue ="0") int page,
        @RequestParam(defaultValue = "20") int size
    ){
        return ResponseEntity.ok(dashboardService.getRunHistory(id, page, size));
    }

    @GetMapping("/dead-letters")
    public ResponseEntity<List<JobRunSummary>> getDeadLetters() {
        return ResponseEntity.ok(dashboardService.getDeadLetters());
    }

    @PostMapping("/dead-letters/{runId}/requeue")
    public ResponseEntity<Void> requeueDeadLetter(@PathVariable UUID runId) {
        dashboardService.requeueDeadLetter(runId);
        return ResponseEntity.noContent().build();
    }
}
