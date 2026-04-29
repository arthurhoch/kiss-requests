package io.github.arthurhoch.kissrequests;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrencyTest {
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
    void shouldLimitConcurrentRequests() throws Exception {
        AtomicInteger activeRequests = new AtomicInteger();
        AtomicInteger maxActiveRequests = new AtomicInteger();
        HttpServer limitedServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        ExecutorService serverExecutor = Executors.newCachedThreadPool();
        limitedServer.setExecutor(serverExecutor);
        limitedServer.createContext("/limited", exchange -> {
            int active = activeRequests.incrementAndGet();
            maxActiveRequests.updateAndGet(current -> Math.max(current, active));
            try {
                Thread.sleep(150);
                byte[] body = "OK".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(e);
            } finally {
                activeRequests.decrementAndGet();
                exchange.close();
            }
        });
        limitedServer.start();

        Http http = Http.builder()
                .maxConcurrentRequests(2)
                .requestTimeout(Duration.ofSeconds(5))
                .build();

        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<Future<HttpResult>> futures = new ArrayList<>();
        String limitedUrl = "http://localhost:" + limitedServer.getAddress().getPort() + "/limited";

        try {
            for (int i = 0; i < 8; i++) {
                futures.add(executor.submit(() ->
                        http.request(HttpMethod.GET, limitedUrl).execute()));
            }

            for (Future<HttpResult> future : futures) {
                HttpResult result = future.get(30, TimeUnit.SECONDS);
                assertEquals(200, result.statusCode());
            }

            assertTrue(maxActiveRequests.get() <= 2,
                    "Expected at most 2 active requests, got " + maxActiveRequests.get());
        } finally {
            executor.shutdownNow();
            limitedServer.stop(0);
            serverExecutor.shutdownNow();
        }
    }

    @Test
    void shouldAllowUnlimitedByDefault() throws Exception {
        Http http = Http.create();

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<HttpResult>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < 10; i++) {
                futures.add(executor.submit(() ->
                        http.request(HttpMethod.GET, baseUrl + "/echo").execute()));
            }

            for (Future<HttpResult> future : futures) {
                HttpResult result = future.get(30, TimeUnit.SECONDS);
                assertEquals(200, result.statusCode());
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void shouldRestoreInterruptFlagWhenInterruptedWaitingForConcurrencyPermit() throws Exception {
        CountDownLatch firstRequestStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstRequest = new CountDownLatch(1);
        HttpServer holdServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        ExecutorService serverExecutor = Executors.newCachedThreadPool();
        holdServer.setExecutor(serverExecutor);
        holdServer.createContext("/hold", exchange -> {
            firstRequestStarted.countDown();
            try {
                if (!releaseFirstRequest.await(5, TimeUnit.SECONDS)) {
                    throw new IOException("Timed out waiting to release first request");
                }
                byte[] body = "OK".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(e);
            } finally {
                exchange.close();
            }
        });
        holdServer.start();

        Http http = Http.builder()
                .maxConcurrentRequests(1)
                .requestTimeout(Duration.ofSeconds(10))
                .build();

        ExecutorService firstExecutor = Executors.newSingleThreadExecutor();
        AtomicReference<HttpException> failure = new AtomicReference<>();
        AtomicBoolean interruptFlagRestored = new AtomicBoolean();
        String holdUrl = "http://localhost:" + holdServer.getAddress().getPort() + "/hold";

        try {
            Future<HttpResult> firstRequest = firstExecutor.submit(() ->
                    http.request(HttpMethod.GET, holdUrl).execute());
            assertTrue(firstRequestStarted.await(5, TimeUnit.SECONDS));

            Thread waitingThread = new Thread(() -> {
                try {
                    http.request(HttpMethod.GET, holdUrl).execute();
                    fail("Expected interrupted semaphore acquire to throw HttpException");
                } catch (HttpException e) {
                    failure.set(e);
                    interruptFlagRestored.set(Thread.currentThread().isInterrupted());
                }
            });
            waitingThread.start();

            waitUntilThreadBlocks(waitingThread);
            waitingThread.interrupt();
            waitingThread.join(5000);

            assertFalse(waitingThread.isAlive());
            assertNotNull(failure.get());
            assertInstanceOf(InterruptedException.class, failure.get().rootCause());
            assertTrue(interruptFlagRestored.get());

            releaseFirstRequest.countDown();
            assertEquals(200, firstRequest.get(5, TimeUnit.SECONDS).statusCode());
        } finally {
            releaseFirstRequest.countDown();
            firstExecutor.shutdownNow();
            holdServer.stop(0);
            serverExecutor.shutdownNow();
        }
    }

    private static void waitUntilThreadBlocks(Thread thread) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            Thread.State state = thread.getState();
            if (state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING) {
                return;
            }
            Thread.sleep(10);
        }
        fail("Thread did not block waiting for the concurrency permit");
    }
}
