package com.shaluyadav.taskscheduler.exceptions;

public class CyclicDependencyException extends RuntimeException{
    public CyclicDependencyException(String message){
        super(message);
    }
}
