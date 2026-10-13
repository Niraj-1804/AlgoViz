package com.algoviz.progress;

import com.fasterxml.jackson.databind.*;
import java.util.*;
import org.springframework.util.MultiValueMap;

public final class Validation {
  private static final ObjectMapper JSON = new ObjectMapper();

  static ApiException bad() {
    return new ApiException(
        400, "invalid-request", "Invalid JSON fields, identifier, header or filter.");
  }

  static JsonNode body(byte[] raw, String... fields) {
    if (raw == null) throw bad();
    if (raw.length > 16384)
      throw new ApiException(413, "body-too-large", "Request exceeds 16 KiB.");
    JsonNode n;
    try {
      n = JSON.reader().with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readTree(raw);
    } catch (Exception e) {
      throw bad();
    }
    if (n == null || !n.isObject() || n.size() != fields.length) throw bad();
    for (String f : fields) if (!positive(n.get(f))) throw bad();
    return n;
  }

  static boolean positive(JsonNode n) {
    return n != null && n.isIntegralNumber() && n.canConvertToInt() && n.intValue() > 0;
  }

  static void key(String key) {
    if (key == null || !key.matches("[A-Za-z0-9._:-]{1,128}")) throw bad();
  }

  static int filters(MultiValueMap<String, String> q) {
    if (!q.containsKey("userId")) throw bad();
    for (var e : q.entrySet())
      if (!Set.of("userId", "status").contains(e.getKey()) || e.getValue().size() != 1) throw bad();
    String u = q.getFirst("userId");
    if (u == null || !u.matches("[0-9]+")) throw bad();
    int id;
    try {
      id = Integer.parseInt(u);
    } catch (Exception e) {
      throw bad();
    }
    if (id <= 0) throw bad();
    if (q.containsKey("status")
        && !Set.of("IN_PROGRESS", "COMPLETED").contains(q.getFirst("status"))) throw bad();
    return id;
  }
}
