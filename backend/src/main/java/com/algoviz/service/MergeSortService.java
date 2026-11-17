
package com.algoviz.service;

import com.algoviz.model.AlgorithmResponse;
import com.algoviz.model.AlgorithmStep;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MergeSortService {

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

        mergeSort(values, 0, values.size() - 1, steps);

        steps.add(new AlgorithmStep(
                List.copyOf(values), -1, -1,
                "DONE", "Array is sorted in ascending order."));

        return new AlgorithmResponse(
                "Merge Sort", "O(n log n)", "O(n)", steps);
    }

    private void mergeSort(
            List<Integer> values,
            int left,
            int right,
            List<AlgorithmStep> steps) {

        if (left >= right) {
            return;
        }

        int mid = left + (right - left) / 2;

        steps.add(new AlgorithmStep(
                List.copyOf(values), left, right,
                "DIVIDE",
                "Divide the array from index " + left
                        + " to " + right + "."));

        mergeSort(values, left, mid, steps);
        mergeSort(values, mid + 1, right, steps);

        merge(values, left, mid, right, steps);
    }

    private void merge(
            List<Integer> values,
            int left,
            int mid,
            int right,
            List<AlgorithmStep> steps) {

        List<Integer> temp = new ArrayList<>();

        int i = left;
        int j = mid + 1;

        while (i <= mid && j <= right) {
            steps.add(new AlgorithmStep(
                    List.copyOf(values), i, j,
                    "COMPARE",
                    values.get(i) + " and " + values.get(j)
                            + " are compared."));

            if (values.get(i) <= values.get(j)) {
                temp.add(values.get(i));
                i++;
            } else {
                temp.add(values.get(j));
                j++;
            }
        }

        while (i <= mid) {
            temp.add(values.get(i));
            i++;
        }

        while (j <= right) {
            temp.add(values.get(j));
            j++;
        }

        for (int k = 0; k < temp.size(); k++) {
            values.set(left + k, temp.get(k));

            steps.add(new AlgorithmStep(
                    List.copyOf(values), left + k, -1,
                    "MERGE",
                    "Place " + temp.get(k)
                            + " at index " + (left + k) + "."));
        }
    }
}
