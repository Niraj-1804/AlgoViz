package com.algoviz.progress;

import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class ProgressStore {
  public record Receipt(String fingerprint, ProgressRecord.PublicRecord body, String location) {}

  public record Collection(List<ProgressRecord.PublicRecord> items, int count) {}

  private final Map<String, ProgressRecord> records = new LinkedHashMap<>();
  private final Map<String, Receipt> receipts = new HashMap<>();
  private final VisualizationClient client;

  public ProgressStore(VisualizationClient client) {
    this.client = client;
  }

  public synchronized Receipt create(int user, int algorithm, int visualization, String key) {
    String fingerprint = user + ":" + algorithm + ":" + visualization;
    Receipt old = receipts.get(key);
    if (old != null) {
      if (!old.fingerprint().equals(fingerprint))
        throw new ApiException(
            409, "idempotency-conflict", "Key already used with different data.");
      return old;
    }
    if (records.values().stream().anyMatch(r -> r.userId == user && r.algorithmId == algorithm))
      throw new ApiException(
          409, "progress-conflict", "Progress already exists for this user and algorithm.");
    verify(client.fetch(visualization), user, algorithm, false);
    ProgressRecord record =
        new ProgressRecord(records.size() + 1, user, algorithm, visualization, key, fingerprint);
    Receipt receipt =
        new Receipt(fingerprint, record.representation(), "/progress-records/" + record.publicId);
    records.put(record.publicId, record);
    receipts.put(key, receipt);
    return receipt;
  }

  private ProgressRecord require(String id) {
    var r = records.get(id);
    if (r == null) throw new ApiException(404, "not-found", "Progress record not found.");
    return r;
  }

  public synchronized ProgressRecord.PublicRecord get(String id) {
    return require(id).representation();
  }

  public synchronized Collection list(int user, String status) {
    var items =
        records.values().stream()
            .filter(r -> r.userId == user && (status == null || r.status.equals(status)))
            .map(ProgressRecord::representation)
            .toList();
    return new Collection(items, items.size());
  }

  public synchronized ProgressRecord.PublicRecord complete(String id, int visualization) {
    var r = require(id);
    if (r.status.equals("COMPLETED")) {
      if (r.visualizationId != visualization)
        throw new ApiException(
            409, "completion-conflict", "Progress already completed with another visualization.");
      return r.representation();
    }
    verify(client.fetch(visualization), r.userId, r.algorithmId, true);
    r.status = "COMPLETED";
    r.visualizationId = visualization;
    r.completedAt = Instant.now().toString();
    return r.representation();
  }

  private void verify(VisualizationClient.Session s, int user, int algorithm, boolean completing) {
    if (s.userId() != user
        || s.algorithmId() != algorithm
        || s.status().equals("FAILED")
        || (completing && !s.status().equals("COMPLETED")))
      throw new ApiException(
          422, "invalid-progress", "Visualization is not eligible for this progress operation.");
  }
}
