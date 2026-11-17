
package com.algoviz.service;

import com.algoviz.model.AlgorithmResponse;
import com.algoviz.model.AlgorithmStep;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SelectionSortService {

    public AlgorithmResponse sort(List<Integer> input) {
        if (input == null || input.isEmpty() || input.size() > 40) {
            throw new IllegalArgumentException(
                    "Please provide between 1 and 40 numbers.");
        }

        List<Integer> values = new ArrayList<>(input);
        List<AlgorithmStep> steps = new ArrayList<>();

        steps.add(new AlgorithmStep(
                List.copyOf(values), -1, -1,
                "START", "We begin with the original array."));

        for (int i = 0; i < values.size() - 1; i++) {
            int minIndex = i;

            for (int j = i + 1; j < values.size(); j++) {
                steps.add(new AlgorithmStep(
                        List.copyOf(values), minIndex, j,
                        "COMPARE",
                        values.get(minIndex) + " and " + values.get(j)
                                + " are compared."));

                if (values.get(j) < values.get(minIndex)) {
                    minIndex = j;
                }
            }

            if (minIndex != i) {
                int temp = values.get(i);
                values.set(i, values.get(minIndex));
                values.set(minIndex, temp);

                steps.add(new AlgorithmStep(
                        List.copyOf(values), i, minIndex,
                        "SWAP", "The minimum value is moved into position."));
            }
        }

        steps.add(new AlgorithmStep(
                List.copyOf(values), -1, -1,
                "DONE", "Array is sorted in ascending order."));

        return new AlgorithmResponse(
                "Selection Sort", "O(n²)", "O(1)", steps);
    }
}
