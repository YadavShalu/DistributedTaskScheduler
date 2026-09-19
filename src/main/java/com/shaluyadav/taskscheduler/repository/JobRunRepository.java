package com.shaluyadav.taskscheduler.repository;

import com.shaluyadav.taskscheduler.entity.JobRun;
import com.shaluyadav.taskscheduler.entity.RunStatus;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;


public interface JobRunRepository extends JpaRepository<JobRun,UUID>{
    Optional<JobRun> findTopByJobIdOrderByCreatedAtDesc(UUID jobId);

    List<JobRun> findByJobIdOrderByCreatedAtDesc(UUID jobId, Pageable pageable);

    List<JobRun> findByStatus(RunStatus status);
}
