package com.algoviz.model;

import java.util.List;

public record AlgorithmStep(
    List<Integer> values,
    int activeIndex,
    int compareIndex,
    String operation,
    String explanation
) {}
