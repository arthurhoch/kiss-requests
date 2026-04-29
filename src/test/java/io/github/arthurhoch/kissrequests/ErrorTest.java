package io.github.arthurhoch.kissrequests;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ErrorTest {
    private static HttpServer server;
    private static String baseUrl;
    private Http http;

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

    @BeforeEach
    void setup() {
        http = Http.create();
    }

    @Test
    void shouldIncludeMethodInException() {
        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("GET", baseUrl + "/status/404").execute());
        assertEquals("GET", ex.method());
    }

    @Test
    void shouldIncludeUrlInException() {
        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("GET", baseUrl + "/status/404").execute());
        assertTrue(ex.url().contains("/status/404"));
    }

    @Test
    void shouldIncludeCurlInException() {
        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("POST", baseUrl + "/status/500",
                        Map.of("Content-Type", "application/json"),
                        "{\"test\":true}").execute());
        assertTrue(ex.curl().contains("curl"));
        assertTrue(ex.curl().contains("POST"));
    }

    @Test
    void shouldIncludeAttemptsInException() {
        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("GET", baseUrl + "/status/500").execute());
        assertFalse(ex.attempts().isEmpty());
        assertEquals(1, ex.attempts().get(0).attemptNumber());
    }

    @Test
    void shouldIncludeResponseBodyInException() {
        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("GET", baseUrl + "/status/404").execute());
        assertNotNull(ex.responseBody());
        assertTrue(ex.responseBody().contains("404"));
    }

    @Test
    void shouldIncludeStatusCodeInException() {
        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("GET", baseUrl + "/status/503").execute());
        assertEquals(503, ex.statusCode());
    }

    @Test
    void shouldIncludeResponseHeadersInException() {
        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("GET", baseUrl + "/status/404").execute());

        boolean foundStatusHeader = ex.responseHeaders().entrySet().stream()
                .anyMatch(entry -> entry.getKey().equalsIgnoreCase("X-Test-Status")
                        && entry.getValue().contains("404"));

        assertTrue(foundStatusHeader);
    }

    @Test
    void shouldIncludeTotalDurationInException() {
        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("GET", baseUrl + "/status/500").execute());
        assertTrue(ex.totalDuration().toMillis() >= 0);
    }

    @Test
    void shouldIncludeNullRootCauseForStatusError() {
        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("GET", baseUrl + "/status/404").execute());
        assertNull(ex.rootCause());
    }

    @Test
    void shouldTruncateLargeResponseBody() {
        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("GET", baseUrl + "/large-error").execute());
        assertTrue(ex.responseBody().length() <= 4200);
        assertTrue(ex.responseBody().contains("[truncated]"));
    }

    @Test
    void shouldProduceReadableReport() {
        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("GET", baseUrl + "/status/404").execute());
        String report = ex.report();
        assertTrue(report.contains("404"));
        assertTrue(report.contains("GET"));
        assertTrue(report.contains("Curl:"));
        assertTrue(report.contains("Attempt 1"));
    }

    @Test
    void shouldIncludeResponseHeadersOnSuccess() {
        HttpResult result = http.request("GET", baseUrl + "/echo").execute();
        assertNotNull(result.headers());
        assertFalse(result.headers().isEmpty());
    }
}
