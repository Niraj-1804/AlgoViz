
package com.algoviz.model;

import java.util.List;

public record SearchingStep(
        List<Integer> values,
        int activeIndex,
        String operation,
        String explanation
) {}
