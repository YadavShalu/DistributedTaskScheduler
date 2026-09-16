package com.shaluyadav.taskscheduler.algorithm;

import java.util.concurrent.ThreadLocalRandom;

public class BackoffCalculator {

    private final long baseDelayMillis;

    private final long maxDelayMillis;

    public BackoffCalculator(long baseDelayMillis, long maxDelayMillis){
        this.baseDelayMillis = baseDelayMillis;
        this.maxDelayMillis = maxDelayMillis;
    }

    public long nextDelayMillis(int attemptNumber){
        double exponential = baseDelayMillis * Math.pow(2, attemptNumber);

        long capped = (long)Math.min(exponential, (double)maxDelayMillis);

        if(capped<=0)return 0;

        return ThreadLocalRandom.current().nextLong(capped);
    }
    
}
