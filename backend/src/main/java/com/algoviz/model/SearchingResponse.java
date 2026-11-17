
package com.algoviz.model;

import java.util.List;

public record SearchingResponse(
        String algorithm,
        int target,
        boolean found,
        int resultIndex,
        String bestTimeComplexity,
        String averageTimeComplexity,
        String worstTimeComplexity,
        String spaceComplexity,
        List<SearchingStep> steps
) {}
