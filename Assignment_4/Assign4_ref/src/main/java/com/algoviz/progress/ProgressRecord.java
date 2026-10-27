package com.algoviz.progress;

import java.time.Instant;
import java.util.UUID;

/** Internal storage model. Never serialized directly by a controller. */
final class ProgressRecord {
  final long internalId;
  final String publicId = UUID.randomUUID().toString();
  final int userId, algorithmId;
  int visualizationId;
  final String idempotencyKey, requestFingerprint;
  final String createdAt = Instant.now().toString();
  String status = "IN_PROGRESS", completedAt;

  ProgressRecord(
      long internalId,
      int userId,
      int algorithmId,
      int visualizationId,
      String key,
      String fingerprint) {
    this.internalId = internalId;
    this.userId = userId;
    this.algorithmId = algorithmId;
    this.visualizationId = visualizationId;
    this.idempotencyKey = key;
    this.requestFingerprint = fingerprint;
  }

  PublicRecord representation() {
    return new PublicRecord(
        publicId, userId, algorithmId, visualizationId, status, createdAt, completedAt);
  }

  public record PublicRecord(
      String id,
      int userId,
      int algorithmId,
      int visualizationId,
      String status,
      String createdAt,
      String completedAt) {}
}
