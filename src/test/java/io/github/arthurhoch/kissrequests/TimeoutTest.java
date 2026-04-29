package io.github.arthurhoch.kissrequests;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class TimeoutTest {
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
    void shouldTimeoutOnSlowResponse() {
        Http http = Http.builder()
                .requestTimeout(Duration.ofMillis(100))
                .build();

        HttpException ex = assertThrows(HttpException.class, () ->
                http.request("GET", baseUrl + "/slow").execute());
        assertTrue(ex.totalDuration().toMillis() < 5000);
    }
}
