package com.algoviz.controller;

import com.algoviz.model.AlgorithmResponse;
import com.algoviz.service.BubbleSortService;
import com.algoviz.service.MergeSortService;
import com.algoviz.service.SelectionSortService;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/algorithms")
@CrossOrigin(origins = {"http://localhost:5173",
        "https://your-frontend-domain.vercel.app" })
public class AlgorithmController {

    private final BubbleSortService bubbleSortService;
    private final SelectionSortService selectionSortService;
    private final MergeSortService mergeSortService;

    public AlgorithmController(BubbleSortService bubbleSortService,
                               SelectionSortService selectionSortService,
                               MergeSortService mergeSortService) {

        this.bubbleSortService = bubbleSortService;
        this.selectionSortService = selectionSortService;
        this.mergeSortService = mergeSortService;

    }

    @GetMapping("/bubble-sort")
    public AlgorithmResponse bubbleSort(@RequestParam(defaultValue = "5,1,4,2,8") String values) {
        try {
            List<Integer> numbers = Arrays.stream(values.split(","))
                .map(String::trim).map(Integer::parseInt).toList();
            return bubbleSortService.sort(numbers);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Input must be comma-separated integers, e.g. 5,1,4,2,8.");
        }
    }

    @GetMapping("/selection-sort")
    public AlgorithmResponse selectionSort(
            @RequestParam(defaultValue = "5,1,4,2,8") String values) {
        try {
            List<Integer> numbers = Arrays.stream(values.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList();

            return selectionSortService.sort(numbers);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Input must be comma-separated integers, e.g. 5,1,4,2,8.");
        }
    }

    @GetMapping("/merge-sort")
    public AlgorithmResponse mergeSort(
            @RequestParam(defaultValue = "5,1,4,2,8") String values) {

        try {
            List<Integer> numbers = Arrays.stream(values.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList();

            return mergeSortService.sort(numbers);

        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Input must be comma-separated integers, e.g. 5,1,4,2,8.");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    public java.util.Map<String, String> badRequest(IllegalArgumentException ex) {
        return java.util.Map.of("error", ex.getMessage());
    }
}
