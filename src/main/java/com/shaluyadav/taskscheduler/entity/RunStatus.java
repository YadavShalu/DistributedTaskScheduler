package com.shaluyadav.taskscheduler.entity;

public enum RunStatus {
    PENDING,
    DISPATCHED,
    RUNNING,
    SUCCEEDED,
    FAILED,
    PENDING_RETRY,
    DEAD_LETTERED   
}
