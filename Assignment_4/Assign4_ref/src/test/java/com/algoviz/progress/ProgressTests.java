package com.algoviz.progress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ProgressTests {
  VisualizationClient client;
  ProgressStore store;
  MockMvc mvc;
  final String body = "{\"userId\":28,\"algorithmId\":1,\"visualizationId\":101}";

  @BeforeEach
  void setup() {
    client = mock(VisualizationClient.class);
    when(client.fetch(101)).thenReturn(new VisualizationClient.Session(101, 28, 1, "COMPLETED"));
    store = new ProgressStore(client);
    mvc =
        MockMvcBuilders.standaloneSetup(new ProgressController(store))
            .setControllerAdvice(new ProblemAdvice())
            .build();
  }

  @Test
  void requiredCreate201AndLocation() throws Exception {
    var result =
        mvc.perform(
                post("/progress-records")
                    .header("Idempotency-Key", "one")
                    .contentType("application/json")
                    .content(body))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.internalId").doesNotExist())
            .andReturn();
    String id =
        new ObjectMapper().readTree(result.getResponse().getContentAsString()).get("id").asText();
    assertEquals("/progress-records/" + id, result.getResponse().getHeader("Location"));
  }

  @Test
  void requiredReplayPreservesOriginalAfterCompletion() throws Exception {
    var first = store.create(28, 1, 101, "key");
    store.complete(first.body().id(), 101);
    assertEquals(first, store.create(28, 1, 101, "key"));
    assertEquals("IN_PROGRESS", first.body().status());
    assertEquals(1, store.list(28, null).count());
    verify(client, times(2)).fetch(101);
  }

  @Test
  void requiredMalformed4xx() throws Exception {
    mvc.perform(
            post("/progress-records")
                .header("Idempotency-Key", "bad")
                .contentType("application/json")
                .content("{broken"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
        .andExpect(jsonPath("$.status").value(400));
    verifyNoInteractions(client);
  }

  @Test
  void requiredMissing404() throws Exception {
    mvc.perform(get("/progress-records/missing"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void validationRejectsBooleanExtraAndOverflow() throws Exception {
    for (String b :
        List.of(
            body.replace("28", "true"),
            body.replace("28", "2147483648"),
            body.replace("101}", "101,\"extra\":1}"),
            body + " {}")) {
      mvc.perform(
              post("/progress-records")
                  .header("Idempotency-Key", "x")
                  .contentType("application/json")
                  .content(b))
          .andExpect(status().isBadRequest());
    }
    verifyNoInteractions(client);
  }

  @Test
  void conflictsAndCompletion() throws Exception {
    var first = store.create(28, 1, 101, "key");
    assertEquals(
        409, assertThrows(ApiException.class, () -> store.create(28, 2, 101, "key")).status);
    assertEquals(
        409, assertThrows(ApiException.class, () -> store.create(28, 1, 101, "another")).status);
    var done = store.complete(first.body().id(), 101);
    assertEquals(done, store.complete(first.body().id(), 101));
    assertEquals(
        409, assertThrows(ApiException.class, () -> store.complete(first.body().id(), 104)).status);
    verify(client, times(2)).fetch(101);
  }

  @Test
  void concurrentReplayDoesOneRemoteCall() throws Exception {
    try (var pool = Executors.newFixedThreadPool(8)) {
      List<Callable<ProgressStore.Receipt>> calls = new ArrayList<>();
      for (int i = 0; i < 8; i++) calls.add(() -> store.create(28, 1, 101, "one"));
      var results = pool.invokeAll(calls);
      for (var f : results) assertEquals(results.getFirst().get(), f.get());
    }
    verify(client, times(1)).fetch(101);
  }

  @Test
  void failureCreatesNoRecordAndDoesNotConsumeKey() {
    when(client.fetch(101))
        .thenThrow(VisualizationClient.unavailable())
        .thenReturn(new VisualizationClient.Session(101, 28, 1, "COMPLETED"));
    assertEquals(
        503, assertThrows(ApiException.class, () -> store.create(28, 1, 101, "key")).status);
    assertEquals(0, store.list(28, null).count());
    store.create(28, 1, 101, "key");
    assertEquals(1, store.list(28, null).count());
  }

  @Test
  void retriesThreeTimesWithExponentialJitter() {
    var delays = new ArrayList<Long>();
    var attempts = new int[] {0};
    var c =
        new VisualizationClient(
            "http://localhost:1",
            u -> {
              attempts[0]++;
              return new VisualizationClient.Reply(503, "{}");
            },
            delays::add);
    assertEquals(503, assertThrows(ApiException.class, () -> c.fetch(101)).status);
    assertEquals(3, attempts[0]);
    assertEquals(2, delays.size());
    assertTrue(delays.get(0) >= 100 && delays.get(0) <= 200);
    assertTrue(delays.get(1) >= 200 && delays.get(1) <= 300);
  }

  @Test
  void neverRetriesAny4xx() {
    for (int code : new int[] {400, 401, 404, 409, 429}) {
      int[] count = {0};
      var c =
          new VisualizationClient(
              "http://localhost:1",
              u -> {
                count[0]++;
                return new VisualizationClient.Reply(code, "{}");
              },
              ms -> fail("4xx must not retry"));
      assertEquals(
          code == 404 ? 422 : 502, assertThrows(ApiException.class, () -> c.fetch(101)).status);
      assertEquals(1, count[0]);
    }
  }

  @Test
  void transportFailuresRetryAndMalformedSuccessDoesNot() {
    int[] count = {0};
    var c =
        new VisualizationClient(
            "http://localhost:1",
            u -> {
              count[0]++;
              throw new IOException("connection");
            },
            ms -> {});
    assertEquals(503, assertThrows(ApiException.class, () -> c.fetch(101)).status);
    assertEquals(3, count[0]);
    var bad =
        new VisualizationClient(
            "http://localhost:1",
            u -> new VisualizationClient.Reply(200, "{\"broken\":true}"),
            ms -> fail());
    assertEquals(502, assertThrows(ApiException.class, () -> bad.fetch(101)).status);
  }

  @Test
  void runningSessionCannotCompleteAndFiltersAreValidated() throws Exception {
    when(client.fetch(102)).thenReturn(new VisualizationClient.Session(102, 28, 2, "RUNNING"));
    var r = store.create(28, 2, 102, "running");
    assertEquals(
        422, assertThrows(ApiException.class, () -> store.complete(r.body().id(), 102)).status);
    assertEquals("IN_PROGRESS", store.get(r.body().id()).status());
    mvc.perform(get("/progress-records?userId=28&status=IN_PROGRESS"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.count").value(1));
    mvc.perform(get("/progress-records?userId=28&userId=29")).andExpect(status().isBadRequest());
  }

  @Test
  void unsupportedMediaTypesReturn415BeforeAnyWork() throws Exception {
    for (String type : List.of("text/plain", "application/xml", "application/octet-stream")) {
      mvc.perform(post("/progress-records").header("Idempotency-Key", "wrong-type")
              .contentType(type).content(body))
          .andExpect(status().isUnsupportedMediaType())
          .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
          .andExpect(jsonPath("$.status").value(415))
          .andExpect(jsonPath("$.type").value("https://algoviz.example/problems/unsupported-media-type"))
          .andExpect(jsonPath("$.title").value("Unsupported Media Type"))
          .andExpect(jsonPath("$.detail").value("Content-Type must be application/json."));
      mvc.perform(put("/progress-records/missing/completion").contentType(type)
              .content("{\"visualizationId\":101}"))
          .andExpect(status().isUnsupportedMediaType())
          .andExpect(jsonPath("$.status").value(415));
    }
    assertEquals(0, store.list(28, null).count());
    verifyNoInteractions(client);
  }

  @Test
  void jsonWithCharsetStillCreatesAndCompletes() throws Exception {
    var response = mvc.perform(post("/progress-records").header("Idempotency-Key", "charset")
            .contentType("application/json; charset=UTF-8").content(body))
        .andExpect(status().isCreated()).andReturn().getResponse();
    mvc.perform(put(response.getHeader("Location") + "/completion")
            .contentType("application/json; charset=UTF-8").content("{\"visualizationId\":101}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"));
  }
}
