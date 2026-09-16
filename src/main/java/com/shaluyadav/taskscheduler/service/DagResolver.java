package com.shaluyadav.taskscheduler.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.shaluyadav.taskscheduler.entity.JobRun;
import com.shaluyadav.taskscheduler.entity.RunStatus;
import com.shaluyadav.taskscheduler.repository.JobDependencyRepository;
import com.shaluyadav.taskscheduler.repository.JobRunRepository;

@Service 
public class DagResolver {
    private final JobDependencyRepository dependencyRepository;
    private final JobRunRepository jobRunRepository;

    public DagResolver(JobDependencyRepository dependencyRepository,
                        JobRunRepository jobRunRepository){
        this.dependencyRepository = dependencyRepository;
        this.jobRunRepository = jobRunRepository;
    }

    public boolean isReadyToDispatch(UUID jobId){
        List<UUID> parentIds = dependencyRepository.findParentIds(jobId);

        if(parentIds.isEmpty())return true;

        return parentIds.stream().allMatch(this::hasSucceeded);
    }

    public boolean hasSucceeded(UUID parentJobId){
        Optional<JobRun> mostRecentRun = jobRunRepository.findTopByJobOrderByCreatedAtDesc(parentJobId);

        return mostRecentRun
            .map(run -> run.getStatus() == RunStatus.SUCCEEDED)
            .orElse(false);
    }

    public List<UUID> findChildrenToReevaluate(UUID succeededJobId){
        return dependencyRepository.findChildIds(succeededJobId);
    }

}
