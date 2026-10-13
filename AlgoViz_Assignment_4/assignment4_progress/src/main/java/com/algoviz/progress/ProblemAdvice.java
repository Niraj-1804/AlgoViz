package com.algoviz.progress;

import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class ProblemAdvice {
  public record Problem(String type, String title, int status, String detail) {}

  static ResponseEntity<Problem> problem(int status, String slug, String detail) {
    return ResponseEntity.status(status)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .cacheControl(CacheControl.noStore())
        .body(
            new Problem(
                "https://algoviz.example/problems/" + slug,
                HttpStatus.valueOf(status).getReasonPhrase(),
                status,
                detail));
  }

  @ExceptionHandler(ApiException.class)
  ResponseEntity<Problem> domain(ApiException e) {
    return problem(e.status, e.slug, e.getMessage());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<Problem> malformed(Exception e) {
    return problem(400, "invalid-request", "A valid JSON body is required.");
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  ResponseEntity<Problem> unsupportedMediaType(HttpMediaTypeNotSupportedException e) {
    return problem(415, "unsupported-media-type", "Content-Type must be application/json.");
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  ResponseEntity<Problem> missing(Exception e) {
    return problem(404, "not-found", "Resource not found.");
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  ResponseEntity<Problem> method(HttpRequestMethodNotSupportedException e) {
    var response = problem(405, "method-not-allowed", "Method not allowed.");
    var headers = new HttpHeaders();
    headers.putAll(response.getHeaders());
    headers.setAllow(e.getSupportedHttpMethods());
    return new ResponseEntity<>(response.getBody(), headers, response.getStatusCode());
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<Problem> unexpected(Exception e) {
    return problem(500, "internal-error", "An unexpected error occurred.");
  }
}
