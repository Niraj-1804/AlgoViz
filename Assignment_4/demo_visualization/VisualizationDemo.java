import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/** Separate Java process: seeded fixture for getVisualization(), not an execution engine. */
public class VisualizationDemo {
  public static void main(String[] args) throws Exception {
    int port = Integer.parseInt(System.getenv().getOrDefault("VISUALIZATION_PORT", "5051"));
    AtomicInteger failures =
        new AtomicInteger(Integer.parseInt(System.getenv().getOrDefault("DEMO_FAIL_FIRST", "0")));
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
    server.createContext(
        "/visualizations/",
        exchange -> {
          String path = exchange.getRequestURI().getPath();
          String id = path.substring(path.lastIndexOf('/') + 1);
          int status = 200;
          String body;
          if (failures.getAndUpdate(n -> Math.max(0, n - 1)) > 0 || id.equals("503")) {
            status = 503;
            body = "{}";
          } else if (id.equals("429")) {
            status = 429;
            body = "{}";
          } else if (id.equals("502")) {
            body = "{\"broken\":true}";
          } else if (java.util.Set.of("101", "102", "103", "104").contains(id)) {
            body =
                "{\"id\":"
                    + id
                    + ",\"userId\":"
                    + (id.equals("103") ? 89 : 28)
                    + ",\"algorithmId\":"
                    + (id.equals("102") ? 2 : 1)
                    + ",\"status\":\""
                    + (id.equals("102") ? "RUNNING" : "COMPLETED")
                    + "\"}";
          } else {
            status = 404;
            body = "{}";
          }
          System.out.println("GET " + path + " " + status);
          byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(status, bytes.length);
          try (var out = exchange.getResponseBody()) {
            out.write(bytes);
          }
        });
    server.start();
    System.out.println("Visualization fixture on " + port);
  }
}
