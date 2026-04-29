package io.github.arthurhoch.kissrequests.internal;

import io.github.arthurhoch.kissrequests.*;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public final class HttpExecutionEngine {
    private static final int ERROR_STATUS_THRESHOLD = 400;

    private final HttpClient httpClient;
    private final HttpConfig config;
    private final ConcurrencyLimiter limiter;

    public HttpExecutionEngine(HttpClient httpClient, HttpConfig config) {
        this.httpClient = httpClient;
        this.config = config;
        this.limiter = new ConcurrencyLimiter(config.maxConcurrentRequests());
    }

    public Object execute(HttpCall<?> call) {
        ConcurrencyLimiter.Token token = null;
        try {
            token = limiter.acquire();
            return doExecute(call);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HttpException(
                    call.method(), call.url(), CurlRenderer.render(call),
                    List.of(new HttpAttempt(1, -1, Duration.ZERO, e.getMessage())),
                    Duration.ZERO, -1, Map.of(), null, e
            );
        } finally {
            if (token != null) token.release();
        }
    }

    private Object doExecute(HttpCall<?> call) {
        List<HttpAttempt> attempts = new ArrayList<>();
        Instant overallStart = Instant.now();
        RetryPolicy retryPolicy = config.retryPolicy();

        for (int attemptNum = 1; attemptNum <= retryPolicy.maxAttempts(); attemptNum++) {
            Instant attemptStart = Instant.now();

            try {
                HttpRequest request = buildRequest(call);
                HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

                Duration attemptDuration = Duration.between(attemptStart, Instant.now());
                int status = response.statusCode();

                if (status >= ERROR_STATUS_THRESHOLD) {
                    String responseBody = readBodyForError(response);
                    attempts.add(new HttpAttempt(attemptNum, status, attemptDuration, "HTTP " + status));

                    if (canRetry(call.method(), status, retryPolicy, attemptNum)) {
                        waitBackoff(attemptNum, retryPolicy);
                        continue;
                    }

                    Duration total = Duration.between(overallStart, Instant.now());
                    throw new HttpException(
                            call.method(), call.url(), CurlRenderer.render(call),
                            attempts, total, status, response.headers().map(),
                            HttpException.truncateBody(responseBody),
                            null
                    );
                }

                attempts.add(new HttpAttempt(attemptNum, status, attemptDuration, null));
                Duration total = Duration.between(overallStart, Instant.now());
                List<HttpAttempt> allAttempts = List.copyOf(attempts);

                return switch (call.callType()) {
                    case TEXT, UPLOAD, MULTIPART -> {
                        String body = readFully(response.body());
                        yield new HttpResult(status, response.headers().map(), body, total, allAttempts, call.method(), call.url());
                    }
                    case DOWNLOAD -> {
                        Path target = call.targetPath();
                        Path parent = target.getParent();
                        if (parent != null && !Files.exists(parent)) {
                            Files.createDirectories(parent);
                        }
                        long bytes = copyToFile(response.body(), target);
                        yield new HttpDownloadResult(status, response.headers().map(), target, bytes, total, allAttempts, call.method(), call.url());
                    }
                    case STREAM -> new HttpStreamResult(status, response.headers().map(), response.body(), total, allAttempts, call.method(), call.url());
                };

            } catch (HttpException e) {
                throw e;
            } catch (IOException e) {
                Duration attemptDuration = Duration.between(attemptStart, Instant.now());
                attempts.add(new HttpAttempt(attemptNum, -1, attemptDuration, e.getMessage()));

                if (canRetryOnTransport(call.method(), retryPolicy, attemptNum)) {
                    waitBackoff(attemptNum, retryPolicy);
                    continue;
                }

                Duration total = Duration.between(overallStart, Instant.now());
                throw new HttpException(
                        call.method(), call.url(), CurlRenderer.render(call),
                        attempts, total, -1, Map.of(), null, e
                );
            } catch (IllegalArgumentException e) {
                Duration attemptDuration = Duration.between(attemptStart, Instant.now());
                attempts.add(new HttpAttempt(attemptNum, -1, attemptDuration, e.getMessage()));
                Duration total = Duration.between(overallStart, Instant.now());
                throw new HttpException(
                        call.method(), call.url(), CurlRenderer.render(call),
                        attempts, total, -1, Map.of(), null, e
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Duration attemptDuration = Duration.between(attemptStart, Instant.now());
                attempts.add(new HttpAttempt(attemptNum, -1, attemptDuration, e.getMessage()));
                Duration total = Duration.between(overallStart, Instant.now());
                throw new HttpException(
                        call.method(), call.url(), CurlRenderer.render(call),
                        attempts, total, -1, Map.of(), null, e
                );
            }
        }

        Duration total = Duration.between(overallStart, Instant.now());
        throw new HttpException(
                call.method(), call.url(), CurlRenderer.render(call),
                attempts, total, -1, Map.of(), null,
                new RuntimeException("All retry attempts exhausted")
        );
    }

    private HttpRequest buildRequest(HttpCall<?> call) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(call.url()))
                .timeout(config.requestTimeout());

        boolean multipart = call.callType() == CallType.MULTIPART;
        for (Map.Entry<String, String> header : call.headers().entrySet()) {
            if (multipart && "Content-Type".equalsIgnoreCase(header.getKey())) {
                continue;
            }
            builder.header(header.getKey(), header.getValue());
        }

        switch (call.callType()) {
            case TEXT -> {
                if (call.body() != null) {
                    builder.method(call.method(), HttpRequest.BodyPublishers.ofString(call.body()));
                } else {
                    builder.method(call.method(), HttpRequest.BodyPublishers.noBody());
                }
            }
            case UPLOAD -> {
                Path file = call.file();
                if (file == null || !Files.exists(file) || !Files.isReadable(file)) {
                    throw new IOException("File not found or not readable: " + file);
                }
                builder.method(call.method(), HttpRequest.BodyPublishers.ofFile(file));
            }
            case DOWNLOAD -> {
                if (call.body() != null) {
                    builder.method(call.method(), HttpRequest.BodyPublishers.ofString(call.body()));
                } else {
                    builder.method(call.method(), HttpRequest.BodyPublishers.noBody());
                }
            }
            case STREAM -> {
                if (call.body() != null) {
                    builder.method(call.method(), HttpRequest.BodyPublishers.ofString(call.body()));
                } else {
                    builder.method(call.method(), HttpRequest.BodyPublishers.noBody());
                }
            }
            case MULTIPART -> {
                String boundary = MultipartBodyBuilder.generateBoundary();
                builder.setHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
                builder.method(call.method(), MultipartBodyBuilder.buildBody(call.fields(), call.fileFields(), boundary));
            }
        }

        return builder.build();
    }

    private boolean canRetry(String method, int status, RetryPolicy policy, int currentAttempt) {
        return currentAttempt < policy.maxAttempts()
                && policy.shouldRetryOnStatus(status)
                && policy.shouldRetryMethod(method);
    }

    private boolean canRetryOnTransport(String method, RetryPolicy policy, int currentAttempt) {
        return currentAttempt < policy.maxAttempts()
                && policy.shouldRetryMethod(method);
    }

    private void waitBackoff(int attemptNum, RetryPolicy policy) {
        Duration backoff = policy.initialBackoff();
        if (backoff.isZero() || backoff.isNegative()) return;

        long millis = backoff.toMillis() * (1L << (attemptNum - 1));
        millis += (long) (Math.random() * backoff.toMillis());
        millis = Math.min(millis, 30_000);

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String readBodyForError(HttpResponse<InputStream> response) {
        try (InputStream is = response.body()) {
            return readWithLimit(is, 8192);
        } catch (IOException e) {
            return null;
        }
    }

    private String readWithLimit(InputStream is, int limit) throws IOException {
        byte[] buffer = new byte[limit];
        int total = 0;
        int read;
        while ((read = is.read(buffer, total, buffer.length - total)) != -1) {
            total += read;
            if (total >= buffer.length) break;
        }
        return new String(buffer, 0, total, java.nio.charset.StandardCharsets.UTF_8);
    }

    private String readFully(InputStream is) throws IOException {
        try (InputStream stream = is) {
            return new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private long copyToFile(InputStream is, Path target) throws IOException {
        try (InputStream stream = is;
             OutputStream out = Files.newOutputStream(target)) {
            return stream.transferTo(out);
        }
    }
}
