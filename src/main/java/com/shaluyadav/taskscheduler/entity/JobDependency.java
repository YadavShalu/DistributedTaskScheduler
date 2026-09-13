package com.shaluyadav.taskscheduler.entity;


import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

import java.io.Serializable;

import java.util.Objects;
import java.util.UUID;


import lombok.Setter;
import lombok.Getter;
import lombok.NoArgsConstructor;




@Entity 
@Table(name = "job_dependencies")
@Getter
@Setter
@NoArgsConstructor
public class JobDependency {
    
    @EmbeddedId 
    private JobDependencyId id;

    public JobDependency(UUID jobId, UUID dependsOnJobId){
        this.id = new JobDependencyId(jobId, dependsOnJobId);
    }

    public UUID getJobId(){
        return id.getJobId();
    }

    @Embeddable 
    @Getter 
    @Setter 
    @NoArgsConstructor
    public static class JobDependencyId implements Serializable {

        @Column(name = "job_id")
        private UUID jobId;

        @Column(name = "depends_on_job_id")
        private UUID dependsOnJobId;

        public JobDependencyId(UUID jobId, UUID dependsOnJobId){
            this.jobId = jobId;
            this.dependsOnJobId = dependsOnJobId;
        }

        @Override 
        public boolean equals(Object o){
            if(this == o) return true;

            if(!(o instanceof JobDependencyId that)) return false;

            return Objects.equals(jobId, that.jobId) 
                && Objects.equals(dependsOnJobId, that.dependsOnJobId); 

        }

        @Override 
        public int hashCode(){
            return Objects.hash(jobId, dependsOnJobId);
        }
    }
}
