package com.algoviz.progress;

import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/progress-records")
public class ProgressController {
  private final ProgressStore store;

  public ProgressController(ProgressStore store) {
    this.store = store;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> create(
      @RequestHeader(value = "Idempotency-Key", required = false) String key,
      @RequestBody(required = false) byte[] raw) {
    var b = Validation.body(raw, "userId", "algorithmId", "visualizationId");
    Validation.key(key);
    var receipt =
        store.create(
            b.get("userId").intValue(),
            b.get("algorithmId").intValue(),
            b.get("visualizationId").intValue(),
            key);
    return ResponseEntity.status(201)
        .header("Location", receipt.location())
        .cacheControl(CacheControl.noStore())
        .body(receipt.body());
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> get(@PathVariable String id) {
    return ok(store.get(id));
  }

  @GetMapping
  public ResponseEntity<?> list(@RequestParam MultiValueMap<String, String> query) {
    int user = Validation.filters(query);
    return ok(store.list(user, query.getFirst("status")));
  }

  @PutMapping(value = "/{id}/completion", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> complete(
      @PathVariable String id, @RequestBody(required = false) byte[] raw) {
    var b = Validation.body(raw, "visualizationId");
    return ok(store.complete(id, b.get("visualizationId").intValue()));
  }

  private ResponseEntity<?> ok(Object o) {
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(o);
  }
}
