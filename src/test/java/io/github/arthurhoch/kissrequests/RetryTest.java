package io.github.arthurhoch.kissrequests;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RetryTest {
    private static HttpServer server;
    private static String baseUrl;

    @BeforeAll
    static void startServer() throws Exception {
        server = TestServer.create();
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @Test
    void shouldRetryOnRetryableStatus() {
        Http http = Http.builder()
                .retryPolicy(RetryPolicy.of(3, Duration.ofMillis(50)))
                .build();

        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("GET", baseUrl + "/status/500").execute());
        assertEquals(500, ex.statusCode());
        assertEquals(3, ex.attempts().size());
    }

    @Test
    void shouldNotRetryOnNonRetryableStatus() {
        Http http = Http.builder()
                .retryPolicy(RetryPolicy.of(3, Duration.ofMillis(50)))
                .build();

        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("GET", baseUrl + "/status/404").execute());
        assertEquals(404, ex.statusCode());
        assertEquals(1, ex.attempts().size());
    }

    @Test
    void shouldNotRetryPostByDefault() {
        Http http = Http.builder()
                .retryPolicy(RetryPolicy.of(3, Duration.ofMillis(50)))
                .build();

        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("POST", baseUrl + "/status/500",
                        Map.of(), "body").execute());
        assertEquals(500, ex.statusCode());
        assertEquals(1, ex.attempts().size());
    }

    @Test
    void shouldRespectMaxAttempts() {
        Http http = Http.builder()
                .retryPolicy(RetryPolicy.of(2, Duration.ofMillis(50)))
                .build();

        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("GET", baseUrl + "/status/503").execute());
        assertEquals(2, ex.attempts().size());
    }

    @Test
    void shouldRecordRetryAttempts() {
        Http http = Http.builder()
                .retryPolicy(RetryPolicy.of(3, Duration.ofMillis(50)))
                .build();

        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("GET", baseUrl + "/status/502").execute());

        assertEquals(3, ex.attempts().size());
        assertEquals(1, ex.attempts().get(0).attemptNumber());
        assertEquals(2, ex.attempts().get(1).attemptNumber());
        assertEquals(3, ex.attempts().get(2).attemptNumber());

        for (HttpAttempt attempt : ex.attempts()) {
            assertEquals(502, attempt.statusCode());
            assertTrue(attempt.duration().toMillis() >= 0);
            assertNotNull(attempt.failureMessage());
        }
    }

    @Test
    void shouldRetryOn429() {
        Http http = Http.builder()
                .retryPolicy(RetryPolicy.of(2, Duration.ofMillis(50)))
                .build();

        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("GET", baseUrl + "/status/429").execute());
        assertEquals(2, ex.attempts().size());
    }

    @Test
    void shouldRetryCustomMethodAndStatus() {
        Http http = Http.builder()
                .retryPolicy(RetryPolicy.of(2, Duration.ofMillis(50), Set.of(500), Set.of("POST")))
                .build();

        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("POST", baseUrl + "/status/500",
                        Map.of(), "body").execute());
        assertEquals(2, ex.attempts().size());
    }

    @Test
    void noRetryByDefault() {
        Http http = Http.create();

        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("GET", baseUrl + "/status/500").execute());
        assertEquals(1, ex.attempts().size());
    }
}
