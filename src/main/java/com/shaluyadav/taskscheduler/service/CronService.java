package com.shaluyadav.taskscheduler.service;

import java.time.ZonedDateTime;

import org.springframework.stereotype.Service;
import com.cronutils.model.Cron;
import com.cronutils.parser.CronParser;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;


@Service
public class CronService {
    public final CronParser parser = new CronParser(
        CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)
    );

    public ZonedDateTime nextFireTime(String cronExpression, ZonedDateTime from){
        Cron cron = parser.parse(cronExpression);
        cron.validate();
        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        return executionTime.nextExecution(from)
            .orElseThrow(() -> new IllegalArgumentException("No future execution for: " + cronExpression));

    }
    

    
}
