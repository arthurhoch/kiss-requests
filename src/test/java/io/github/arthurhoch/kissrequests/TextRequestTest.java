package io.github.arthurhoch.kissrequests;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TextRequestTest {
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
    void shouldExecuteGetRequest() {
        HttpResult result = http.request("GET", baseUrl + "/echo").execute();
        assertEquals(200, result.statusCode());
        assertTrue(result.body().startsWith("GET"));
        assertNotNull(result.headers());
        assertTrue(result.duration().toMillis() >= 0);
        assertEquals(1, result.attempts().size());
        assertEquals("GET", result.method());
        assertTrue(result.url().contains("/echo"));
    }

    @Test
    void shouldExecutePostWithBody() {
        HttpResult result = http.request("POST", baseUrl + "/echo",
                Map.of("Content-Type", "application/json"),
                "{\"name\":\"Arthur\"}").execute();
        assertEquals(200, result.statusCode());
        assertTrue(result.body().contains("Arthur"));
    }

    @Test
    void shouldExecutePutRequest() {
        HttpResult result = http.request("PUT", baseUrl + "/echo",
                Map.of(), "put body").execute();
        assertEquals(200, result.statusCode());
        assertTrue(result.body().startsWith("PUT"));
        assertTrue(result.body().contains("put body"));
    }

    @Test
    void shouldExecuteDeleteRequest() {
        HttpResult result = http.request("DELETE", baseUrl + "/echo").execute();
        assertEquals(200, result.statusCode());
        assertTrue(result.body().startsWith("DELETE"));
    }

    @Test
    void shouldExecutePatchRequest() {
        HttpResult result = http.request("PATCH", baseUrl + "/echo",
                Map.of(), "patch").execute();
        assertEquals(200, result.statusCode());
        assertTrue(result.body().startsWith("PATCH"));
    }

    @Test
    void shouldExecuteHeadRequest() {
        HttpResult result = http.request("HEAD", baseUrl + "/echo").execute();
        assertEquals(200, result.statusCode());
    }

    @Test
    void shouldExecuteOptionsRequest() {
        HttpResult result = http.request("OPTIONS", baseUrl + "/echo").execute();
        assertEquals(200, result.statusCode());
    }

    @Test
    void shouldSendHeaders() {
        HttpResult result = http.request("GET", baseUrl + "/headers",
                Map.of("X-Custom", "test-value")).execute();
        assertEquals(200, result.statusCode());
        assertTrue(result.body().contains("test-value"));
    }

    @Test
    void shouldHandleNullBody() {
        HttpResult result = http.request("GET", baseUrl + "/echo",
                Map.of(), null).execute();
        assertEquals(200, result.statusCode());
    }

    @Test
    void shouldHandleNullHeaders() {
        HttpResult result = http.request("POST", baseUrl + "/echo",
                null, "body").execute();
        assertEquals(200, result.statusCode());
    }

    @Test
    void shouldRejectNonSuccessStatus() {
        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("GET", baseUrl + "/status/404").execute());
        assertEquals(404, ex.statusCode());
        assertTrue(ex.getMessage().contains("404"));
    }

    @Test
    void shouldRejectInternalServerError() {
        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("GET", baseUrl + "/status/500").execute());
        assertEquals(500, ex.statusCode());
    }

    @Test
    void shouldThrowOnNullMethod() {
        assertThrows(NullPointerException.class, () ->
                http.request(null, baseUrl + "/echo"));
    }

    @Test
    void shouldThrowOnNullUrl() {
        assertThrows(NullPointerException.class, () ->
                http.request("GET", null));
    }

    @Test
    void shouldThrowOnBlankMethod() {
        assertThrows(IllegalArgumentException.class, () ->
                http.request("  ", baseUrl + "/echo"));
    }

    @Test
    void shouldThrowOnBlankUrl() {
        assertThrows(IllegalArgumentException.class, () ->
                http.request("GET", "  "));
    }

    @Test
    void shouldWrapInvalidUrlInHttpExceptionOnExecute() {
        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("GET", "not a url").execute());
        assertEquals(-1, ex.statusCode());
        assertTrue(ex.rootCause() instanceof IllegalArgumentException);
        assertTrue(ex.curl().contains("not a url"));
    }

    @Test
    void shouldSnapshotHeadersWhenPreparingCall() {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Original", "one");

        HttpCall<HttpResult> call = http.request("GET", baseUrl + "/headers", headers);
        headers.put("X-Original", "two");
        headers.put("X-New", "new");

        HttpResult result = call.execute();
        assertTrue(result.body().contains("X-original: one"));
        assertFalse(result.body().contains("two"));
        assertFalse(result.body().contains("X-new"));
    }

    @Test
    void shouldWorkWithHttpMethodConstants() {
        HttpResult result = http.request(HttpMethod.GET, baseUrl + "/echo").execute();
        assertEquals(200, result.statusCode());
    }
}
