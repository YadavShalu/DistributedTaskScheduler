package com.shaluyadav.taskscheduler.service;

import com.shaluyadav.taskscheduler.algorithm.BackoffCalculator;
import com.shaluyadav.taskscheduler.dispatch.JobDispatcher;
import com.shaluyadav.taskscheduler.entity.Job;
import com.shaluyadav.taskscheduler.entity.JobRun;
import com.shaluyadav.taskscheduler.entity.RunStatus;
import com.shaluyadav.taskscheduler.repository.JobRepository;
import com.shaluyadav.taskscheduler.repository.JobRunRepository;

import jakarta.annotation.PostConstruct;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RetryService {
    private static final String RETRY_ZSET_KET = "retry_queue";
    private static final String DEAD_LETTERED_LIST_KEY = "dead_lettered_queue";


    @Value("${scheduler.backoff-base-ms:10000}")
    private long backoffBaseMs;

    @Value("${scheduler.backOff-max-ms:300000}")
    private long backoffMaxMs;

    private final JobRunRepository jobRunRepository;
    private final JobRepository jobRepository;
    private final DagResolver dagResolver;
    private final JobDispatcher jobDispatcher;
    private final StringRedisTemplate redisTemplate;

    private BackoffCalculator backoffCalculator;

    public RetryService(JobRunRepository jobRunRepository,
                         JobRepository jobRepository,
                         DagResolver dagResolver,
                         JobDispatcher jobDispatcher,
                         StringRedisTemplate redisTemplate) {
        this.jobRunRepository = jobRunRepository;
        this.jobRepository = jobRepository;
        this.dagResolver = dagResolver;
        this.jobDispatcher = jobDispatcher;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void initBackoffCalculator() {
        this.backoffCalculator = new BackoffCalculator(backoffBaseMs, backoffMaxMs);
    }

    public void handleCompletion(UUID runId, boolean success, String errorMessage) {
        JobRun run = jobRunRepository.findById(runId)
            .orElseThrow(() -> new NoSuchElementException("No JobRun found with id " + runId));

        if (success) {
            handleSuccess(run);
        } else {
            handleFailure(run, errorMessage);
        }
    }

    private void handleSuccess(JobRun run){
        run.setStatus(RunStatus.SUCCEEDED);
        run.setFinishedAt(Instant.now());
        jobRunRepository.save(run);
        log.info("Run {} for job {} succeeded", run.getId(), run.getJobId());

        for(UUID childId : dagResolver.findChildrenToReevaluate(run.getJobId())){
            if(!dagResolver.isReadyToDispatch(childId)){
                continue;
            }

            Job childJob = jobRepository.findById(childId).orElse(null);
            if(childJob == null || !childJob.isEnabled()){
                continue;
            }

            log.info("Job {} is now unblocked by parent {} succeeding -- dispatching",
                childId, run.getJobId());

            jobDispatcher.dispatch(childJob, 0);
        }
    }

    private void handleFailure(JobRun run , String errorMessage){
        Job job = jobRepository.findById(run.getJobId())
            .orElseThrow(() -> new NoSuchElementException("No Job found with id "+ run.getJobId()));
        
        int nextAttemptNumber = run.getAttempt()+1;

        if(nextAttemptNumber >= job.getMaxRetries()){
            run.setStatus(RunStatus.DEAD_LETTERED);
            run.setErrorMessage(errorMessage != null ? errorMessage : "Unknown failure");
            run.setFinishedAt(Instant.now());
            jobRunRepository.save(run);

            redisTemplate.opsForList().leftPush(DEAD_LETTERED_LIST_KEY, run.getId().toString());
            log.warn("Run {} for job {} exhausted {} retries -- moved to dead-letter queue",
                run.getId(), job.getId(), job.getMaxRetries());
            return;
        }

        run.setStatus(RunStatus.PENDING_RETRY);
        run.setErrorMessage(errorMessage != null ? errorMessage : "Unkknow failure");
        run.setFinishedAt(Instant.now());
        jobRunRepository.save(run);

        long delayMillis = backoffCalculator.nextDelayMillis(run.getAttempt());
        long retryAtEpochMillis = Instant.now().toEpochMilli() + delayMillis;

        redisTemplate.opsForZSet().add(RETRY_ZSET_KET, run.getId().toString(), retryAtEpochMillis);
        log.info("Run {} for job {} failed (attempt {}) -- retry {} scheduled in {}ms",
            run.getId(), job.getId(), run.getAttempt(), nextAttemptNumber, delayMillis);

    }

    @Scheduled(fixedDelay = 1000)
    public void drainDueRetries(){
        long now = Instant.now().toEpochMilli();

        Set<String> dueRunIds = redisTemplate.opsForZSet()
            .rangeByScore(RETRY_ZSET_KET, Double.NEGATIVE_INFINITY, now);

        if(dueRunIds == null || dueRunIds.isEmpty()){
            return ;
        }

        for(String runIdString : dueRunIds){
            redisTemplate.opsForZSet().remove(RETRY_ZSET_KET, runIdString);
            retryOne(UUID.fromString(runIdString));
        }
    }

    private void retryOne(UUID previousRunId){
        JobRun previousRun = jobRunRepository.findById(previousRunId).orElse(null);
        if(previousRun == null)
        {
            log.warn("Retry queue referenced JobRun {} which no longer exists -- skipping", previousRunId);
            return;
        }

        Job job = jobRepository.findById(previousRun.getJobId()).orElse(null);
        if(job == null || !job.isEnabled()){
            log.info("Skipping retry for job {} -- not found or disabled", previousRun.getJobId());
            return;
        }

        jobDispatcher.dispatch(job, previousRun.getAttempt() + 1);
    }
}
