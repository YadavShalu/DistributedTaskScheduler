package com.shaluyadav.taskscheduler.service;

import com.shaluyadav.taskscheduler.exceptions.InvalidCronExpressionException;

import java.time.ZonedDateTime;

import java.util.Optional;
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

    public void validate(String cronExpression){
        try{
            Cron cron = parser.parse(cronExpression);
            cron.validate();

        }catch(IllegalArgumentException e){
            throw new InvalidCronExpressionException(
                "Invalid cron expression '"+ cronExpression + "': " + e.getMessage(), e
            );
        }
    }

    public ZonedDateTime nextFireTime(String cronExpression, ZonedDateTime from){
        Cron cron = parser.parse(cronExpression);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        Optional<ZonedDateTime> next= executionTime.nextExecution(from);
        return next.orElseThrow(() -> new InvalidCronExpressionException(
            "Cron expression '"+ cronExpression + "' has no future execution after "+ from));
        
    }

    
}
