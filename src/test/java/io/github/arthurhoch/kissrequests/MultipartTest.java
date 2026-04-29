package io.github.arthurhoch.kissrequests;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MultipartTest {
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
    void shouldSendMultipartWithTextFieldsOnly() {
        HttpResult result = http.multipart("POST", baseUrl + "/multipart",
                Map.of(), Map.of("name", "Arthur", "email", "arthur@example.com"), Map.of()).execute();
        assertEquals(200, result.statusCode());
        assertTrue(result.body().contains("parts=2"));
    }

    @Test
    void shouldSendMultipartWithFilesOnly() throws IOException {
        Path tempFile = Files.createTempFile("multipart-test", ".txt");
        try {
            Files.writeString(tempFile, "file content here");
            HttpResult result = http.multipart("POST", baseUrl + "/multipart",
                    Map.of(), Map.of(), Map.of("document", tempFile)).execute();
            assertEquals(200, result.statusCode());
            assertTrue(result.body().contains("parts=1"));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void shouldSendMultipartWithBothFieldsAndFiles() throws IOException {
        Path tempFile = Files.createTempFile("multipart-both", ".txt");
        try {
            Files.writeString(tempFile, "hello");
            HttpResult result = http.multipart("POST", baseUrl + "/multipart",
                    Map.of("Authorization", "Bearer token"),
                    Map.of("name", "Arthur"),
                    Map.of("file", tempFile)).execute();
            assertEquals(200, result.statusCode());
            assertTrue(result.body().contains("parts=2"));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void shouldSetMultipartContentTypeWithBoundary() {
        String curl = http.multipart("POST", baseUrl + "/multipart",
                Map.of(), Map.of("name", "test"), Map.of()).toCurl();
        assertTrue(curl.contains("-F"));
        assertTrue(curl.contains("name=test"));
    }

    @Test
    void shouldIgnoreUserContentTypeForMultipartBoundary() {
        HttpResult result = http.multipart("POST", baseUrl + "/multipart",
                Map.of("Content-Type", "text/plain"),
                Map.of("name", "Arthur"),
                Map.of()).execute();
        assertEquals(200, result.statusCode());
        assertTrue(result.body().contains("boundary=----KissRequestsBoundary"));
        assertTrue(result.body().contains("parts=1"));

        String curl = http.multipart("POST", baseUrl + "/multipart",
                Map.of("Content-Type", "text/plain"),
                Map.of("name", "Arthur"),
                Map.of()).toCurl();
        assertFalse(curl.contains("Content-Type: text/plain"));
    }

    @Test
    void shouldFailMultipartForMissingFile() {
        Path missing = Path.of("/nonexistent/file.dat");
        HttpException ex = assertThrows(HttpException.class, () ->
                http.multipart("POST", baseUrl + "/multipart",
                        Map.of(), Map.of(), Map.of("file", missing)).execute());
        assertNotNull(ex.rootCause());
    }
}
