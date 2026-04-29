package io.github.arthurhoch.kissrequests;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StreamTest {
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
    void shouldStreamResponse() throws Exception {
        HttpStreamResult result = http.stream("GET", baseUrl + "/echo",
                Map.of(), null).execute();
        assertEquals(200, result.statusCode());
        assertNotNull(result.inputStream());
        assertTrue(result.duration().toMillis() >= 0);
    }

    @Test
    void shouldReadStreamContent() throws Exception {
        HttpStreamResult result = http.stream("GET", baseUrl + "/echo",
                Map.of(), null).execute();
        try (InputStream is = result.inputStream()) {
            byte[] bytes = is.readAllBytes();
            String body = new String(bytes, StandardCharsets.UTF_8);
            assertTrue(body.startsWith("GET"));
        }
    }

    @Test
    void shouldCloseStream() throws Exception {
        HttpStreamResult result = http.stream("GET", baseUrl + "/echo",
                Map.of(), null).execute();
        InputStream is = result.inputStream();
        is.close();
        assertDoesNotThrow(is::close);
    }
}
