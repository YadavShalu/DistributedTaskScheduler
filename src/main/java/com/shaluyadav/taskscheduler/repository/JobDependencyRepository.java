package com.shaluyadav.taskscheduler.repository;

import org.springframework.stereotype.Repository;

@Repository 
public interface JobDependencyRepository extends JpaRepository<JobDependency, JobDependencyId> {
    
    
}
