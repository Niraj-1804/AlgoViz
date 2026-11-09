package com.algoviz.visualization;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api")
public class VisualizationController {
    private final VisualizationRepository repo;
    private final AlgorithmService algorithms;
    public VisualizationController(VisualizationRepository repo,AlgorithmService algorithms){this.repo=repo;this.algorithms=algorithms;}

    public record VisualizationRequest(String algorithm,List<Integer> values,Integer target) {}

    @GetMapping("/algorithms")
    public List<String> listAlgorithms(){return List.of("bubble-sort","linear-search");}

    @GetMapping("/visualizations")
    public List<Visualization> listVisualizations(){return repo.findAll();}

    @PostMapping("/visualizations")
    public ResponseEntity<?> create(@RequestBody VisualizationRequest request){
        var result=algorithms.run(request.algorithm(),request.values(),request.target());
        var saved=repo.save(new Visualization(request.algorithm(),result.result().toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id",saved.getId(),"visualization",result));
    }

    @DeleteMapping("/visualizations/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "not found"));
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badInput(IllegalArgumentException ex){
        return ResponseEntity.badRequest().body(Map.of("error",ex.getMessage()));
    }
}
