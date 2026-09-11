package com.shaluyadav.taskscheduler.repository;

import com.shaluyadav.taskscheduler.entity.Job;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository 
public interface JobRepository extends JpaRepository<Job, UUID> {
    List<Job> findByEnabledTrue();
}
