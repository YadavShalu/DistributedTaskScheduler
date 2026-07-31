package com.shaluyadav.task_scheduler.entity;
import jakarta.persistence.*;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.Type;
import com.vladmihalcea.hibernate.type.json.JsonType;

@Entity
@Table(name = "job")
public class Job {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private String cronExpression;
    private String handler;
    @Type(JsonType.class)
    @Column(columnDefinition="jsonb")
    private Map<String,Object> handlerConfig;
    private int max_retries=5;
    private int timeoutSeconds = 300;
    private boolean enabled = true;
}


