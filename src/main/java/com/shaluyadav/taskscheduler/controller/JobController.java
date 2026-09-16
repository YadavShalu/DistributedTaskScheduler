package com.shaluyadav.taskscheduler.controller;

import com.shaluyadav.taskscheduler.service.;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping("/jobs")
public class JobController {
    private final JobService jobService;
    
    
}
