package com.shaluyadav.taskscheduler.dispatch;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.SortedMap;
import java.util.TreeMap;


public class ConsistentHashRing {

    private final SortedMap<Long, String> ring = new TreeMap<>();

    private final int virtualNodesPerWorker;

    public ConsistentHashRing(int virtualNodesPerWorker){
        this.virtualNodesPerWorker = virtualNodesPerWorker;
    }

    public void addWorker(String workerId){
        for(int i=0 ; i<virtualNodesPerWorker ; i++){
            long point = hash(workerId + "-v"+i);
            ring.put(point, workerId);
        }
    }
    public void removeWorker(String workerId) {
        for (int i = 0; i < virtualNodesPerWorker; i++) {
            long point = hash(workerId + "-v" + i);
            ring.remove(point);
        }
    }

    public String getWorkerFor(String jobId) {
        if (ring.isEmpty()) {
            throw new IllegalStateException("No workers registered in the consistent hash ring");
        }

        long jobHash = hash(jobId);

        SortedMap<Long, String> clockwisePortion = ring.tailMap(jobHash);

        
        Long targetPoint = clockwisePortion.isEmpty()
            ? ring.firstKey()
            : clockwisePortion.firstKey();

        return ring.get(targetPoint);
    }

    private long hash(String key) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
    
            byte[] digestBytes = md5.digest(key.getBytes(StandardCharsets.UTF_8));

            return ((long) (digestBytes[0] & 0xFF) << 24)
                 | ((digestBytes[1] & 0xFF) << 16)
                 | ((digestBytes[2] & 0xFF) << 8)
                 |  (digestBytes[3] & 0xFF);
        } catch (NoSuchAlgorithmException e) {
           
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    
    }
}
