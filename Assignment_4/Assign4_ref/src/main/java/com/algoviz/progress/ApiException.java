package com.algoviz.progress;

public class ApiException extends RuntimeException {
  final int status;
  final String slug;

  public ApiException(int status, String slug, String detail) {
    super(detail);
    this.status = status;
    this.slug = slug;
  }
}
