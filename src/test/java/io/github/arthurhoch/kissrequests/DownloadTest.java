package io.github.arthurhoch.kissrequests;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DownloadTest {
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
    void shouldDownloadToFile() throws Exception {
        Path target = Files.createTempFile("download-test", ".bin");
        try {
            HttpDownloadResult result = http.download("GET", baseUrl + "/download",
                    Map.of(), target).execute();
            assertEquals(200, result.statusCode());
            assertTrue(result.bytesWritten() > 0);
            assertEquals(target, result.file());
            assertTrue(Files.size(target) > 0);
        } finally {
            Files.deleteIfExists(target);
        }
    }

    @Test
    void shouldVerifyDownloadedContent() throws Exception {
        Path target = Files.createTempFile("download-verify", ".bin");
        try {
            HttpDownloadResult result = http.download("GET", baseUrl + "/download",
                    Map.of(), target).execute();
            assertEquals(1024, result.bytesWritten());
            assertEquals(1024, Files.size(target));
        } finally {
            Files.deleteIfExists(target);
        }
    }

    @Test
    void shouldFailDownloadForNonSuccessStatus() {
        Path target = Path.of("/tmp/should-not-exist.bin");
        HttpException ex = assertThrows(HttpException.class, () ->
                http.download("GET", baseUrl + "/status/404",
                        Map.of(), target).execute());
        assertEquals(404, ex.statusCode());
    }

    @Test
    void shouldCreateParentDirectories() throws Exception {
        Path tempDir = Files.createTempDirectory("download-parent-test");
        Path target = tempDir.resolve("subdir").resolve("file.bin");
        try {
            HttpDownloadResult result = http.download("GET", baseUrl + "/download",
                    Map.of(), target).execute();
            assertEquals(200, result.statusCode());
            assertTrue(Files.exists(target));
        } finally {
            Files.deleteIfExists(target);
            Files.deleteIfExists(target.getParent());
            Files.deleteIfExists(tempDir);
        }
    }
}
