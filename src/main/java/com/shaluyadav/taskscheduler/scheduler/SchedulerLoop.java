package com.shaluyadav.taskscheduler.scheduler;

import com.shaluyadav.taskscheduler.dispatch.JobDispatcher;
import com.shaluyadav.taskscheduler.entity.Job;
import com.shaluyadav.taskscheduler.repository.JobRepository;
import com.shaluyadav.taskscheduler.service.DagResolver;
import com.shaluyadav.taskscheduler.service.JobService;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import lombok.extern.slf4j.Slf4j;

@Component 
@Slf4j 
public class SchedulerLoop {
    private final PriorityQueue<ScheduledEntry>heap = new PriorityQueue<>((a,b) -> Long.compare(a.fireTimeMillis(), b.fireTimeMillis()));

    private final ReentrantLock heapLock = new ReentrantLock();

    private final JobRepository jobRepository;
    private final JobService jobService;
    private final DagResolver dagResolver;
    private final JobDispatcher jobDispatcher;

    public SchedulerLoop(JobRepository jobRepository,
                          JobService jobService,
                          DagResolver dagResolver,
                          JobDispatcher jobDispatcher) {
        this.jobRepository = jobRepository;
        this.jobService = jobService;
        this.dagResolver = dagResolver;
        this.jobDispatcher = jobDispatcher;
    }

    public void register(UUID jobId, ZonedDateTime fireTime){
        if(fireTime == null){
            return;
        }

        long fireTimeMillis = fireTime.toInstant().toEpochMilli();

        heapLock.lock();

        try{
            heap.offer(new ScheduledEntry(jobId, fireTimeMillis));
        }finally{
            heapLock.unlock();
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void tick(){
        long now = Instant.now().toEpochMilli();

        heapLock.lock();
        try{
            while(true){
                ScheduledEntry soonest = heap.peek();
                if(soonest == null || soonest.fireTimeMillis() > now){
                    break;
                }
                heap.poll();

                dispatchAndReschedule(soonest.jobId());

            }
        }finally{
            heapLock.unlock();
        }
    }

    private void dispatchAndReschedule(UUID jobId){
        Job job = jobRepository.findById(jobId).orElse(null);
        if(job == null || !job.isEnabled()){
            log.info("Skipping dispatch for job {} -- not found or disabled", jobId);
            rescheduleIfCronJob(job);
            return ; 
        }

        if(!dagResolver.isReadyToDispatch(job.getId())){
            log.info("Job {} is due but not ready -- one or more dependencies have not succeeded yet", job.getId());
            rescheduleIfCronJob(job);
            return ;
        }

        jobDispatcher.dispatch(job, 0);
        rescheduleIfCronJob(job);
    }

    private void rescheduleIfCronJob(Job job){
        if(job==null)return;

        ZonedDateTime nextFireTime = jobService.computeNextFireTime(job);
        register(job.getId(), nextFireTime);
    }
    
}
