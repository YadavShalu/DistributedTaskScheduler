package com.shaluyadav.taskscheduler.algorithm;

import com.shaluyadav.taskscheduler.exceptions.CyclicDependencyException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.springframework.stereotype.Component;


@Component 
public class TopologicalSort {
    
    public List<String>sort(Map<String, List<String>>adjacency){
        Map<String, Integer> inDegree = new HashMap<>();

        for(String node : adjacency.keySet()){
            inDegree.putIfAbsent(node, 0);
        }

        for(List<String> children : adjacency.values()){
            for(String child : children){
                inDegree.merge(child, 1, Integer::sum);
            }
        }

        Queue<String> ready = new ArrayDeque<>();
        for(Map.Entry<String,Integer> entry : inDegree.entrySet()){
            if(entry.getValue() == 0){
                ready.offer(entry.getKey());
            }
        }

        List<String> order = new ArrayList<>();

        while(!ready.isEmpty()){
            String node = ready.poll();
            order.add(node);

            for(String child : adjacency.getOrDefault(node,List.of())){
                int updatedDegree = inDegree.merge(child, -1, Integer::sum);
                if(updatedDegree == 0){
                    ready.offer(child);
                }
                
            }

        }

        if(order.size() != inDegree.size()){
            throw new CyclicDependencyException(
                "Cycle detected in job dependecy graph --cannot determine a valid execution order");
            
        }

        return order;
    }
}
