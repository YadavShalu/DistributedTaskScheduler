package com.shaluyadav.taskscheduler.service;

import com.shaluyadav.taskscheduler.dto.JobRunSummary;
import com.shaluyadav.taskscheduler.dto.JobSummary;
import com.shaluyadav.taskscheduler.dispatch.JobDispatcher;
import com.shaluyadav.taskscheduler.entity.Job;
import com.shaluyadav.taskscheduler.entity.JobRun;
import com.shaluyadav.taskscheduler.repository.JobRepository;
import com.shaluyadav.taskscheduler.repository.JobRunRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final String DEAD_LETTER_LIST_KEY  = "dead_letter_queue";
    private final JobRepository jobRepository;
    private final JobRunRepository jobRunRepository;
    private final JobService jobService;
    private final JobDispatcher jobDispatcher;
    private final StringRedisTemplate redisTemplate;


    public DashboardService(JobRepository jobRepository,
                             JobRunRepository jobRunRepository,
                             JobService jobService,
                             JobDispatcher jobDispatcher,
                             StringRedisTemplate redisTemplate) {
        this.jobRepository = jobRepository;
        this.jobRunRepository = jobRunRepository;
        this.jobService = jobService;
        this.jobDispatcher = jobDispatcher;
        this.redisTemplate = redisTemplate;
    }

    public List<JobSummary> listJobs(){
        List<Job>allJobs = jobRepository.findAll();

        return allJobs.stream().map(this::buildSummary)
        .collect(Collectors.toList());
    }

    private JobSummary buildSummary(Job job) {
        Optional<JobRun> lastRun = jobRunRepository.findTopByJobOrderByCreatedAtDesc(job.getId());

        return JobSummary.builder()
            .id(job.getId())
            .name(job.getName())
            .cronExpression(job.getCronExpression())
            .handlerType(job.getHandlerType())
            .enabled(job.isEnabled())
            .lastRunStatus(lastRun.map(run -> run.getStatus().name()).orElse(null))
            .lastRunAt(lastRun.map(JobRun::getCreatedAt).orElse(null))
            .nextFireTime(jobService.computeNextFireTime(job))
            .build();
    }

    public List<JobRunSummary> getRunHistory(UUID jobId, int page, int size) {

        if (!jobRepository.existsById(jobId)) {
            throw new NoSuchElementException("No job found with id " + jobId);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<JobRun> runs = jobRunRepository.findByJobIdOrderByCreatedAtDesc(jobId, pageable);

        return runs.stream()
            .map(run -> toSummary(run, null))
            .collect(Collectors.toList());
    }

    public List<JobRunSummary> getDeadLetters() {
        List<String> runIdStrings = redisTemplate.opsForList().range(DEAD_LETTER_LIST_KEY, 0, -1);
        if (runIdStrings == null || runIdStrings.isEmpty()) {
            return List.of();
        }

        List<UUID> runIds = runIdStrings.stream().map(UUID::fromString).collect(Collectors.toList());

    
        List<JobRun> runs = jobRunRepository.findAllById(runIds);

        
        List<UUID> jobIds = runs.stream().map(JobRun::getJobId).distinct().collect(Collectors.toList());
        Map<UUID, String> jobNamesById = jobRepository.findAllById(jobIds).stream()
            .collect(Collectors.toMap(Job::getId, Job::getName));

        List<JobRunSummary> summaries = new ArrayList<>();
        for (JobRun run : runs) {
            String jobName = jobNamesById.get(run.getJobId());
            summaries.add(toSummary(run, jobName));
        }
        return summaries;
    }

    public void requeueDeadLetter(UUID runId) {
        JobRun deadRun = jobRunRepository.findById(runId)
            .orElseThrow(() -> new NoSuchElementException("No JobRun found with id " + runId));

        Job job = jobRepository.findById(deadRun.getJobId())
            .orElseThrow(() -> new NoSuchElementException("No Job found with id " + deadRun.getJobId()));
        redisTemplate.opsForList().remove(DEAD_LETTER_LIST_KEY, 1, runId.toString());

        jobDispatcher.dispatch(job, 0);
    }

    private JobRunSummary toSummary(JobRun run, String jobName) {
        return JobRunSummary.builder()
            .id(run.getId())
            .jobId(run.getJobId())
            .jobName(jobName)
            .status(run.getStatus())
            .attempt(run.getAttempt())
            .workerId(run.getWorkerId())
            .scheduledFor(run.getScheduledFor())
            .startedAt(run.getStartedAt())
            .finishedAt(run.getFinishedAt())
            .errorMessage(run.getErrorMessage())
            .build();
    }
    
}
