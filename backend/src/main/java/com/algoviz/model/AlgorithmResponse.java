package com.algoviz.model;

import java.util.List;

public record AlgorithmResponse(
    String algorithm,
    String timeComplexity,
    String spaceComplexity,
    List<AlgorithmStep> steps
) {}
