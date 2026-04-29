package io.github.arthurhoch.kissrequests;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class CurlTest {
    private final Http http = Http.create();

    @Test
    void shouldRenderCurlForGet() {
        String curl = http.request("GET", "https://api.example.com/users").toCurl();
        assertTrue(curl.contains("curl"));
        assertTrue(curl.contains("https://api.example.com/users"));
        assertFalse(curl.contains("-X"));
        assertFalse(curl.contains("-d"));
    }

    @Test
    void shouldRenderCurlForPostWithBody() {
        String curl = http.request("POST", "https://api.example.com/users",
                Map.of("Content-Type", "application/json"),
                "{\"name\":\"Arthur\"}").toCurl();
        assertTrue(curl.contains("-X POST"));
        assertTrue(curl.contains("Content-Type: application/json"));
        assertTrue(curl.contains("--data-raw"));
        assertTrue(curl.contains("Arthur"));
    }

    @Test
    void shouldRenderCurlForUpload() {
        String curl = http.upload("POST", "https://api.example.com/files",
                Map.of("Content-Type", "application/pdf"),
                java.nio.file.Path.of("/tmp/test.pdf")).toCurl();
        assertTrue(curl.contains("--data-binary"));
        assertTrue(curl.contains("@/tmp/test.pdf"));
    }

    @Test
    void shouldRenderCurlForDownload() {
        String curl = http.download("GET", "https://api.example.com/files/123",
                Map.of(), java.nio.file.Path.of("/tmp/out.pdf")).toCurl();
        assertTrue(curl.contains("-o"));
        assertTrue(curl.contains("/tmp/out.pdf"));
    }

    @Test
    void shouldRenderCurlForMultipart() {
        String curl = http.multipart("POST", "https://api.example.com/upload",
                Map.of("Authorization", "Bearer token"),
                Map.of("name", "Arthur"),
                Map.of("file", java.nio.file.Path.of("/tmp/photo.jpg"))).toCurl();
        assertTrue(curl.contains("-F"));
        assertTrue(curl.contains("name=Arthur"));
        assertTrue(curl.contains("file=@/tmp/photo.jpg"));
    }

    @Test
    void shouldEscapeSingleQuotes() {
        String curl = http.request(HttpMethod.POST, "https://api.example.com/it'works",
                Map.of("X-Name", "Arthur's token"), "it's working").toCurl();
        assertTrue(curl.contains("it'\\''works"));
        assertTrue(curl.contains("Arthur'\\''s token"));
        assertTrue(curl.contains("it'\\''s working"));

        String multipartCurl = http.multipart(HttpMethod.POST, "https://api.example.com/forms",
                Map.of("X-Name", "Arthur's upload"),
                Map.of("description", "Contract's final copy"),
                Map.of("file", Path.of("/tmp/Arthur's file.txt"))).toCurl();
        assertTrue(multipartCurl.contains("Arthur'\\''s upload"));
        assertTrue(multipartCurl.contains("Contract'\\''s final copy"));
        assertTrue(multipartCurl.contains("Arthur'\\''s file.txt"));
    }

    @Test
    void shouldNotExecuteWhenRenderingCurl() throws Exception {
        AtomicInteger hits = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/count", exchange -> {
            hits.incrementAndGet();
            byte[] body = "OK".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        try {
            HttpCall<HttpResult> call = http.request(
                    HttpMethod.GET,
                    "http://localhost:" + server.getAddress().getPort() + "/count");

            String curl = call.toCurl();
            String curlBase64 = call.toCurlBase64();
            assertTrue(curl.contains("/count"));
            assertFalse(curlBase64.isBlank());
            assertEquals(0, hits.get());

            assertEquals(200, call.execute().statusCode());
            assertEquals(1, hits.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldRenderCurlWithoutNetworkForUnreachableHost() {
        HttpCall<HttpResult> call = http.request(HttpMethod.GET, "https://nonexistent.invalid/api");
        String curl = call.toCurl();
        assertTrue(curl.contains("nonexistent"));
    }

    @Test
    void shouldReturnBase64OfCurlForTextRequest() {
        HttpCall<HttpResult> call = http.request("POST", "https://api.example.com/users",
                Map.of("Content-Type", "application/json"),
                "{\"name\":\"Arthur\"}");
        String base64 = call.toCurlBase64();
        assertFalse(base64.isBlank());
        String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
        assertEquals(call.toCurl(), decoded);
    }

    @Test
    void shouldReturnBase64OfCurlForUpload() {
        HttpCall<HttpResult> call = http.upload("POST", "https://api.example.com/files",
                Map.of("Content-Type", "application/octet-stream"),
                java.nio.file.Path.of("/tmp/file.bin"));
        String base64 = call.toCurlBase64();
        assertFalse(base64.isBlank());
        String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
        assertEquals(call.toCurl(), decoded);
    }

    @Test
    void shouldReturnBase64OfCurlForDownload() {
        HttpCall<HttpDownloadResult> call = http.download("GET", "https://api.example.com/file.zip",
                Map.of(), java.nio.file.Path.of("/tmp/file.zip"));
        String base64 = call.toCurlBase64();
        assertFalse(base64.isBlank());
        String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
        assertEquals(call.toCurl(), decoded);
    }

    @Test
    void shouldReturnBase64OfCurlForStream() {
        HttpCall<HttpStreamResult> call = http.stream("POST", "https://api.example.com/stream",
                Map.of(), "payload");
        String base64 = call.toCurlBase64();
        assertFalse(base64.isBlank());
        String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
        assertEquals(call.toCurl(), decoded);
    }

    @Test
    void shouldReturnBase64OfCurlForMultipart() {
        HttpCall<HttpResult> call = http.multipart("POST", "https://api.example.com/upload",
                Map.of(), Map.of("name", "test"), Map.of("file", java.nio.file.Path.of("/tmp/f.txt")));
        String base64 = call.toCurlBase64();
        assertFalse(base64.isBlank());
        String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
        assertEquals(call.toCurl(), decoded);
    }

    @Test
    void toCurlBase64ShouldNotExecute() {
        HttpCall<HttpResult> call = http.request("GET", "https://nonexistent.invalid/api");
        String base64 = call.toCurlBase64();
        assertTrue(base64.length() > 0);
    }
}
