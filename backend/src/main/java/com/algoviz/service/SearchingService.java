
package com.algoviz.service;

import com.algoviz.model.SearchingResponse;
import com.algoviz.model.SearchingStep;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchingService {

    public SearchingResponse linearSearch(List<Integer> input, int target) {
        List<Integer> values = validateInput(input);
        List<SearchingStep> steps = new ArrayList<>();

        steps.add(new SearchingStep(
                List.copyOf(values), -1, "START",
                "Linear Search started."));

        for (int i = 0; i < values.size(); i++) {
            steps.add(new SearchingStep(
                    List.copyOf(values), i, "COMPARE",
                    "Checking index " + i + ", value " + values.get(i) + "."));

            if (values.get(i) == target) {
                steps.add(new SearchingStep(
                        List.copyOf(values), i, "FOUND",
                        "Target " + target + " found at index " + i + "."));

                return new SearchingResponse(
                        "Linear Search", target, true, i,
                        "O(1)", "O(n)", "O(n)", "O(1)",
                        List.copyOf(steps));
            }
        }

        steps.add(new SearchingStep(
                List.copyOf(values), -1, "NOT_FOUND",
                "Target " + target + " was not found."));

        return new SearchingResponse(
                "Linear Search", target, false, -1,
                "O(1)", "O(n)", "O(n)", "O(1)",
                List.copyOf(steps));
    }

    public SearchingResponse binarySearch(List<Integer> input, int target) {
        List<Integer> values = validateInput(input);

        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) < values.get(i - 1)) {
                throw new IllegalArgumentException(
                        "Binary Search requires numbers sorted in ascending order.");
            }
        }

        List<SearchingStep> steps = new ArrayList<>();
        steps.add(new SearchingStep(
                List.copyOf(values), -1, "START",
                "Binary Search started on the sorted array."));

        int left = 0;
        int right = values.size() - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            int current = values.get(mid);

            steps.add(new SearchingStep(
                    List.copyOf(values), mid, "COMPARE",
                    "Checking middle index " + mid + ", value " + current + "."));

            if (current == target) {
                steps.add(new SearchingStep(
                        List.copyOf(values), mid, "FOUND",
                        "Target " + target + " found at index " + mid + "."));

                return new SearchingResponse(
                        "Binary Search", target, true, mid,
                        "O(1)", "O(log n)", "O(log n)", "O(1)",
                        List.copyOf(steps));
            }

            if (current < target) {
                left = mid + 1;
                steps.add(new SearchingStep(
                        List.copyOf(values), mid, "MOVE_RIGHT",
                        "Target is larger. Search the right half."));
            } else {
                right = mid - 1;
                steps.add(new SearchingStep(
                        List.copyOf(values), mid, "MOVE_LEFT",
                        "Target is smaller. Search the left half."));
            }
        }

        steps.add(new SearchingStep(
                List.copyOf(values), -1, "NOT_FOUND",
                "Target " + target + " was not found."));

        return new SearchingResponse(
                "Binary Search", target, false, -1,
                "O(1)", "O(log n)", "O(log n)", "O(1)",
                List.copyOf(steps));
    }

    private List<Integer> validateInput(List<Integer> input) {
        if (input == null || input.isEmpty() || input.size() > 40) {
            throw new IllegalArgumentException(
                    "Please provide between 1 and 40 numbers.");
        }

        return new ArrayList<>(input);
    }
}
