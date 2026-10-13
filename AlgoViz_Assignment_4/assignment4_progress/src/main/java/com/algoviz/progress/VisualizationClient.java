package com.algoviz.progress;

import com.fasterxml.jackson.databind.*;
import java.io.IOException;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VisualizationClient {
  public record Session(int id, int userId, int algorithmId, String status) {}

  record Reply(int status, String body) {}

  interface Transport {
    Reply get(URI uri) throws IOException, InterruptedException;
  }

  interface Sleeper {
    void sleep(long ms) throws InterruptedException;
  }

  private final String base;
  private final Transport transport;
  private final Sleeper sleeper;
  private final ObjectMapper json = new ObjectMapper();

  @org.springframework.beans.factory.annotation.Autowired
  public VisualizationClient(@Value("${visualization.base-url}") String base) {
    this(base, realTransport(), Thread::sleep);
  }

  VisualizationClient(String base, Transport transport, Sleeper sleeper) {
    URI uri = URI.create(base);
    if (!Set.of("http", "https").contains(uri.getScheme())
        || uri.getHost() == null
        || uri.getQuery() != null
        || uri.getFragment() != null)
      throw new IllegalArgumentException("VISUALIZATION_BASE_URL must be an absolute HTTP(S) URL");
    this.base = base.replaceAll("/+$", "");
    this.transport = transport;
    this.sleeper = sleeper;
  }

  private static Transport realTransport() {
    HttpClient http =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(500))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
    return uri -> {
      var r =
          http.send(
              HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(1)).GET().build(),
              HttpResponse.BodyHandlers.ofString());
      return new Reply(r.statusCode(), r.body());
    };
  }

  public Session fetch(int id) {
    for (int attempt = 0; attempt < 3; attempt++) {
      try {
        Reply r = transport.get(URI.create(base + "/visualizations/" + id));
        if (Set.of(502, 503, 504).contains(r.status())) {
          if (attempt == 2) throw unavailable();
        } else if (r.status() == 404)
          throw new ApiException(
              422, "invalid-progress", "Referenced visualization does not exist.");
        else if (r.status() != 200) throw dependency();
        else {
          JsonNode n;
          try {
            n = json.readTree(r.body());
          } catch (Exception e) {
            throw dependency();
          }
          if (n == null
              || !n.isObject()
              || !Validation.positive(n.get("id"))
              || n.get("id").intValue() != id
              || !Validation.positive(n.get("userId"))
              || !Validation.positive(n.get("algorithmId"))
              || !n.hasNonNull("status")
              || !n.get("status").isTextual()
              || !Set.of("RUNNING", "COMPLETED", "FAILED").contains(n.get("status").asText()))
            throw dependency();
          return new Session(
              id,
              n.get("userId").intValue(),
              n.get("algorithmId").intValue(),
              n.get("status").asText());
        }
      } catch (IOException e) {
        if (attempt == 2) throw unavailable();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw unavailable();
      }
      long delay = 100L * (1L << attempt) + ThreadLocalRandom.current().nextLong(101);
      System.out.println("Visualization GET retry: delay=" + delay + "ms");
      try {
        sleeper.sleep(delay);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw unavailable();
      }
    }
    throw unavailable();
  }

  static ApiException dependency() {
    return new ApiException(
        502, "dependency-error", "Visualization Service returned an invalid or refused response.");
  }

  static ApiException unavailable() {
    return new ApiException(
        503,
        "dependency-unavailable",
        "Cannot verify learning progress right now. Please try again later.");
  }
}
