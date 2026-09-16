package com.shaluyadav.taskscheduler.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.shaluyadav.taskscheduler.entity.JobDependency;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

@Repository 
public interface JobDependencyRepository extends JpaRepository<JobDependency, JobDependency.JobDependencyId> {

    @Query("SELECT d.id.dependsOnJobId FROM JobDependency d WHERE d.id.jobId = :jobId")
    List<UUID> findParentIds(@Param("jobId") UUID jobId);

    @Query("SELECT d.id.jobId FROM JobDependency d WHERE d.id.dependsOnJobId = :jobId")
    List<UUID> findChildIds(@Param("jobId") UUID jobId);

    @Query("SELECT d from JobDependency d")
    List<JobDependency> findAllEdges();
}
