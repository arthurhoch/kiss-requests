package io.github.arthurhoch.kissrequests;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UploadTest {
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
    void shouldUploadBinaryFile() throws IOException {
        Path tempFile = Files.createTempFile("upload-test", ".bin");
        try {
            byte[] data = new byte[256];
            new java.util.Random(42).nextBytes(data);
            Files.write(tempFile, data);

            HttpResult result = http.upload("POST", baseUrl + "/echo",
                    Map.of("Content-Type", "application/octet-stream"),
                    tempFile).execute();
            assertEquals(200, result.statusCode());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void shouldFailUploadForMissingFile() {
        Path missing = Path.of("/nonexistent/path/file.bin");
        HttpException ex = assertThrows(HttpException.class, () ->
                http.upload("POST", baseUrl + "/echo",
                        Map.of(), missing).execute());
        assertTrue(ex.rootCause() instanceof IOException);
    }

    @Test
    void shouldUploadWithCustomContentType() throws IOException {
        Path tempFile = Files.createTempFile("upload-test", ".txt");
        try {
            Files.writeString(tempFile, "hello world");
            HttpResult result = http.upload("POST", baseUrl + "/echo",
                    Map.of("Content-Type", "text/plain"), tempFile).execute();
            assertEquals(200, result.statusCode());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
