
package com.algoviz.controller;

import com.algoviz.model.SearchingResponse;
import com.algoviz.service.SearchingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://your-frontend-domain.vercel.app"
})
public class SearchingController {

    private final SearchingService searchingService;

    public SearchingController(SearchingService searchingService) {
        this.searchingService = searchingService;
    }

    @GetMapping("/linear")
    public SearchingResponse linearSearch(
            @RequestParam(defaultValue = "5,1,4,2,8") String values,
            @RequestParam int target) {

        return searchingService.linearSearch(parseValues(values), target);
    }

    @GetMapping("/binary")
    public SearchingResponse binarySearch(
            @RequestParam(defaultValue = "1,2,4,5,8") String values,
            @RequestParam int target) {

        return searchingService.binarySearch(parseValues(values), target);
    }

    private List<Integer> parseValues(String values) {
        try {
            if (values == null || values.isBlank()) {
                throw new IllegalArgumentException(
                        "Please provide comma-separated numbers.");
            }

            return Arrays.stream(values.split(",", -1))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList();

        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Input must contain comma-separated integers, e.g. 5,1,4,2,8.");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> badRequest(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }
}
