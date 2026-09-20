package com.shaluyadav.taskscheduler.dispatch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component 
@Slf4j 
public class WorkerRegistry {
    private static final String HEARTBEAT_ZSET_KEY = "worker:heartbeat";
    private static final String URL_HASH_KEY = "workers:urls";

    @Value("${scheduler.worker-heartbeat-ttl-seconds:30}")
    private long heartbeatTtlSeconds;

    private final StringRedisTemplate redisTemplate;

    private final ConsistentHashRing ring = new ConsistentHashRing(150);

    public WorkerRegistry(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void rebuildRingFromRedis() {
        Set<String> currentWorkerIds = getNonExpiredWorkerIds();
        for (String workerId : currentWorkerIds) {
            ring.addWorker(workerId);
        }
        log.info("Rebuilt consistent hash ring from Redis with {} existing workers", currentWorkerIds.size());
    }

    public void registerOrHeartbeat(String workerId, String baseURL) {
        long expiresAtEpochMillis = Instant.now().plusSeconds(heartbeatTtlSeconds).toEpochMilli();

        redisTemplate.opsForZSet().add(HEARTBEAT_ZSET_KEY, workerId, expiresAtEpochMillis);

        redisTemplate.opsForHash().put(URL_HASH_KEY, workerId, baseURL);

        ring.addWorker(workerId);

        log.info("Worker {} registered/heartbeat at {}, expires in {}s", workerId, baseURL, heartbeatTtlSeconds);
    }

    public void deregister(String workerId) {
        redisTemplate.opsForZSet().remove(HEARTBEAT_ZSET_KEY, workerId);
        redisTemplate.opsForHash().delete(URL_HASH_KEY, workerId);
        ring.removeWorker(workerId);
        log.info("Worker {} deregistered", workerId);
    }

    public String getWorkerUrlFor(String jobId) {
        String workerId = ring.getWorkerFor(jobId);
        Object url = redisTemplate.opsForHash().get(URL_HASH_KEY, workerId);
        if (url == null) {
            throw new IllegalStateException(
                "Worker " + workerId + " is in the hash ring but has no registered URL");
        }
        return url.toString();
    }

    @Scheduled(fixedDelay = 10_000)
    public void pruneExpiredWorkers() {
        long now = Instant.now().toEpochMilli();
        Set<String> expired = redisTemplate.opsForZSet()
            .rangeByScore(HEARTBEAT_ZSET_KEY, Double.NEGATIVE_INFINITY, now);

        if (expired == null || expired.isEmpty()) {
            return;
        }

        for (String workerId : expired) {
            log.warn("Worker {} missed its heartbeat deadline -- removing from ring", workerId);
            deregister(workerId);
        }
    }

    private Set<String> getNonExpiredWorkerIds() {
        long now = Instant.now().toEpochMilli();
        Set<String> ids = redisTemplate.opsForZSet()
            .rangeByScore(HEARTBEAT_ZSET_KEY, now, Double.POSITIVE_INFINITY);
        return ids == null ? Set.of() : ids;
    }


}
