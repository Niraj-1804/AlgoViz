package com.algoviz.service;

import com.algoviz.model.AlgorithmResponse;
import com.algoviz.model.AlgorithmStep;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class BubbleSortService {
    public AlgorithmResponse sort(List<Integer> input) {
        if (input == null || input.isEmpty() || input.size() > 40) {
            throw new IllegalArgumentException("Please provide between 1 and 40 numbers.");
        }

        List<Integer> values = new ArrayList<>(input);
        List<AlgorithmStep> steps = new ArrayList<>();
        steps.add(new AlgorithmStep(List.copyOf(values), -1, -1,
            "START", "We begin with the original array."));

        for (int end = values.size() - 1; end > 0; end--) {
            for (int j = 0; j < end; j++) {
                steps.add(new AlgorithmStep(List.copyOf(values), j, j + 1,
                    "COMPARE", values.get(j) + " and " + values.get(j + 1) + " are compared."));
                if (values.get(j) > values.get(j + 1)) {
                    int temp = values.get(j);
                    values.set(j, values.get(j + 1));
                    values.set(j + 1, temp);
                    steps.add(new AlgorithmStep(List.copyOf(values), j, j + 1,
                        "SWAP", "They were out of order, so we swap them."));
                }
            }
        }
        steps.add(new AlgorithmStep(List.copyOf(values), -1, -1,
            "DONE", "Array is sorted in ascending order."));
        return new AlgorithmResponse("Bubble Sort", "O(n²)", "O(n)", steps);
    }
}
