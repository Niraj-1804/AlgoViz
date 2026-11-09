package com.algoviz.visualization;

import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class AlgorithmService {
    public record Step(List<Integer> values, List<Integer> activeIndices, String operation, String explanation) {}
    public record Result(String algorithm, String timeComplexity, String spaceComplexity,
                         List<Step> steps, List<Integer> result) {}

    public Result run(String algorithm,List<Integer> input,Integer target) {
        if (input==null || input.isEmpty() || input.size()>50)
            throw new IllegalArgumentException("values must contain 1 to 50 numbers");
        if (algorithm==null) throw new IllegalArgumentException("algorithm is required");
        List<Integer> a=new ArrayList<>(input);
        List<Step> steps=new ArrayList<>();
        steps.add(new Step(List.copyOf(a),List.of(),"start","Starting values"));
        switch(algorithm) {
            case "bubble-sort" -> {
                for(int i=0;i<a.size();i++) {
                    boolean swapped=false;
                    for(int j=0;j<a.size()-1-i;j++) {
                        steps.add(new Step(List.copyOf(a),List.of(j,j+1),"compare","Compare neighbouring values"));
                        if(a.get(j)>a.get(j+1)) {
                            Collections.swap(a,j,j+1); swapped=true;
                            steps.add(new Step(List.copyOf(a),List.of(j,j+1),"swap","Swap these values"));
                        }
                    }
                    if(!swapped)break;
                }
                steps.add(new Step(List.copyOf(a),List.of(),"done","Array is sorted"));
                return new Result(algorithm,"O(n²) worst / O(n) best","O(1)",steps,a);
            }
            case "linear-search" -> {
                if(target==null) throw new IllegalArgumentException("target required for linear-search");
                for(int i=0;i<a.size();i++) {
                    steps.add(new Step(List.copyOf(a),List.of(i),"compare","Check index "+i));
                    if(a.get(i).equals(target)) {
                        steps.add(new Step(List.copyOf(a),List.of(i),"found","Target found at index "+i));
                        return new Result(algorithm,"O(n)","O(1)",steps,a);
                    }
                }
                steps.add(new Step(List.copyOf(a),List.of(),"not-found","Target not found"));
                return new Result(algorithm,"O(n)","O(1)",steps,a);
            }
            default -> throw new IllegalArgumentException("Supported: bubble-sort, linear-search");
        }
    }
}
