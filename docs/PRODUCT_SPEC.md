# KissRequests — Product Specification

**Status: Implemented / v0.1.0**

## Project Name

KissRequests

## Mission

Make HTTP requests in Java so simple that you can write them from memory, debug them with one method call, and never depend on a heavy library again.

## Problem Statement

Java developers have two choices for HTTP:

1. **JDK `HttpClient`**: Verbose. Making a simple GET requires understanding `HttpRequest.Builder`, `BodyHandlers`, and exception handling. Error messages are generic. There is no easy way to see what was sent.
2. **Third-party libraries** (Apache HttpClient, OkHttp): Powerful but heavy. They bring transitive dependencies, complex configurations, and API surface areas that require documentation to navigate.

Neither choice makes the common case trivial.

KissRequests solves this by wrapping the native JDK `HttpClient` with a tiny, memorable API that:
- Reduces common HTTP calls to one line.
- Provides `toCurl()` for instant debugging.
- Throws rich exceptions with full context.
- Has zero mandatory external dependencies.

## Target Users

1. Java application developers who need to make HTTP requests.
2. Library and CLI authors who want lightweight HTTP transport.
3. Developers who value simplicity and debuggability.
4. Developers who cannot or will not add external HTTP library dependencies.
5. Developers who want to use native JDK capabilities without boilerplate.

## v1 Scope

### Core Features

1. **Text requests**: GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS with String body.
2. **Binary file upload**: Upload a file from `Path` using streaming.
3. **Download to file**: Download response body directly to `Path`.
4. **Stream response**: Return response body as `InputStream` for caller-controlled reading.
5. **Multipart/form-data**: Send form fields and files as `multipart/form-data`.
6. **`toCurl()`**: Render any prepared call as a copyable curl command.
7. **`execute()`**: Execute the prepared call.
8. **Rich errors**: `HttpException` with method, URL, curl, attempts, duration, status code, response body, root cause.

### Configuration (on the `Http` singleton)

1. **Retry policy**: Max attempts, backoff, retry status codes, retry methods.
2. **Timeout**: Connect timeout and request timeout.
3. **Max concurrent requests**: Semaphore-based concurrency limiting.
4. **Executor**: Optional `Executor` for the underlying `HttpClient`.

### Infrastructure

1. **Maven project**: Java 17, zero production dependencies.
2. **Unit tests**: JUnit 5, local HTTP test server, no internet required.
3. **Documentation**: README, docs/, examples, GitHub Pages.
4. **GitHub Actions CI**: Run tests on push and PR.
5. **GitHub Pages**: Jekyll documentation site from `docs/`.
6. **Maven Central publishing**: Sonatype Central Publisher Portal.

## v1 Non-Goals

1. JSON or XML serialization.
2. Annotation-driven API.
3. REST framework features (routing, controllers, etc.).
4. Secret masking or credential management.
5. OAuth helpers or token refresh.
6. Observability integrations (OpenTelemetry, Micrometer).
7. Circuit breaker or resilience patterns.
8. Service discovery.
9. Cache layer.
10. Spring, Quarkus, or any framework integration.
11. Virtual threads as a required feature.
12. Cookie management.
13. Redirect policy customization (beyond what the JDK HttpClient provides).
14. Proxy configuration (in v1; the JDK HttpClient supports it, but KissRequests does not expose it yet).
15. SSL context customization (in v1).

## API Philosophy

1. **One obvious way** to do each thing.
2. **Memorable**: Method and class names should be guessable.
3. **No DSL explosion**: Five operations (request, upload, download, stream, multipart) with minimal overloads.
4. **No annotations**: Configuration is code, not metadata.
5. **No hidden behavior**: What you see is what you get.
6. **Zero mandatory configuration**: `Http.create()` works immediately.
7. **`http.request(...)` prepares, `.execute()` executes**: The mental model is always prepare-then-execute.
8. **`.toCurl()` always available**: Every prepared call can be rendered as curl.

## Public API Examples

### Simple GET

```java
Http http = Http.create();
HttpResult result = http.request(HttpMethod.GET, "https://api.example.com/users").execute();
System.out.println(result.statusCode());
System.out.println(result.body());
```

### POST with body

```java
Http http = Http.create();
HttpResult result = http.request(
        HttpMethod.POST,
        "https://api.example.com/users",
        Map.of("Content-Type", "application/json"),
        "{\"name\":\"Arthur\"}"
).execute();
System.out.println(result.statusCode());
```

### Debug with curl

```java
HttpCall<HttpResult> call = http.request(
        HttpMethod.POST,
        "https://api.example.com/users",
        Map.of("Content-Type", "application/json"),
        "{\"name\":\"Arthur\"}"
);

String curl = call.toCurl();
// curl -X POST 'https://api.example.com/users' -H 'Content-Type: application/json' --data-raw '{"name":"Arthur"}'

HttpResult result = call.execute();
```

### Upload file

```java
HttpResult result = http.upload(HttpMethod.POST, "https://api.example.com/files",
        Map.of("Content-Type", "application/octet-stream"),
        Path.of("document.pdf")).execute();
```

### Download file

```java
HttpDownloadResult result = http.download(HttpMethod.GET, "https://api.example.com/files/123",
        Map.of(),
        Path.of("downloaded.pdf")).execute();
System.out.println("Downloaded " + result.bytesWritten() + " bytes");
```

### Stream response

```java
HttpStreamResult result = http.stream(HttpMethod.GET, "https://api.example.com/stream",
        Map.of(), null).execute();
try (InputStream is = result.inputStream()) {
    is.transferTo(System.out);
}
```

### Multipart form

```java
HttpResult result = http.multipart(HttpMethod.POST, "https://api.example.com/upload",
        Map.of("Authorization", "Bearer token"),
        Map.of("name", "Arthur", "email", "arthur@example.com"),
        Map.of("avatar", Path.of("photo.jpg"))).execute();
```

### Error handling

```java
try {
    HttpResult result = http.request(HttpMethod.GET, "https://api.example.com/missing").execute();
} catch (HttpException e) {
    System.err.println("Failed: " + e.getMessage());
    System.err.println("Status: " + e.statusCode());
    System.err.println("Curl: " + e.curl());
    System.err.println("Attempts: " + e.attempts().size());
    System.err.println("Duration: " + e.totalDuration().toMillis() + "ms");
    System.err.println("Response: " + e.responseBody());
}
```

### Configured singleton

```java
Http http = Http.builder()
        .connectTimeout(Duration.ofSeconds(5))
        .requestTimeout(Duration.ofSeconds(30))
        .retryPolicy(RetryPolicy.of(3, Duration.ofMillis(500)))
        .maxConcurrentRequests(10)
        .build();
```

### Retry inspection

```java
try {
    http.request(HttpMethod.GET, "https://api.example.com/flaky").execute();
} catch (HttpException e) {
    for (HttpAttempt attempt : e.attempts()) {
        System.out.printf("Attempt %d: status=%d, duration=%dms%n",
                attempt.attemptNumber(),
                attempt.statusCode(),
                attempt.duration().toMillis());
    }
}
```

## Expected Behavior

### Execution Model

1. `Http.create()` returns a usable default instance.
2. `http.request(...)` returns an immutable `HttpCall`. No network activity.
3. `call.toCurl()` returns the curl representation. No network activity.
4. `call.execute()` triggers the network call. Returns a result or throws `HttpException`.
5. `Http` is thread-safe. One instance can be shared across threads.
6. `HttpMethod` is a constants class for standard methods. Raw method strings remain accepted for custom or uncommon methods.

### Retry Behavior

1. Default: no retries (1 attempt).
2. When configured, retries on specified status codes with exponential backoff and jitter.
3. POST is NOT retried by default (only idempotent methods: GET, HEAD, OPTIONS, PUT, DELETE).
4. Transport errors (IOException) are also retried.
5. All attempts are tracked and visible in `HttpException`.

### Timeout Behavior

1. Connect timeout: applied to the `HttpClient` builder. Affects connection establishment.
2. Request timeout: applied to each `HttpRequest`. Affects the full request/response cycle.
3. Timeout triggers `HttpException` with status code -1 and the timeout as root cause.

### Error Behavior

1. All execution failures throw `HttpException`.
2. `HttpException` always contains: method, URL, curl, attempts, total duration.
3. When a response was received: also contains status code, response headers, and response body (truncated).
4. When the failure is a transport error: root cause is the `IOException`.
5. Interrupted threads restore the interrupt flag.

### Concurrency Behavior

1. Default: unlimited concurrent requests.
2. When configured, a semaphore limits concurrent requests to the configured maximum.
3. Requests exceeding the limit block until a permit is available.

## Edge Cases

1. **Null URL**: Throw `NullPointerException` or `IllegalArgumentException`.
2. **Empty URL**: Throw `IllegalArgumentException`.
3. **Invalid URL format**: Throw `HttpException` when the JDK rejects it.
4. **Null method**: Throw `NullPointerException` or `IllegalArgumentException`.
5. **Null headers**: Treat as empty map (`Map.of()`).
6. **Null body**: Treat as no body.
7. **Upload file does not exist**: Throw `HttpException` with file path and error.
8. **Download target directory does not exist**: Create parent directories before writing the file.
9. **Multipart file does not exist**: Throw `HttpException` with file path and error.
10. **Large response body in error**: Truncate to 4KB in the exception.
11. **Thread interrupted during execution**: Restore interrupt flag, throw `HttpException`.
12. **Concurrent execution beyond limit**: Block on semaphore, handle interruption.

## Acceptance Criteria

1. `Http.create()` returns a working instance with no configuration.
2. `http.request(HttpMethod.GET, url).execute()` returns an `HttpResult` with status code and body.
3. `http.request(HttpMethod.POST, url, headers, body).execute()` sends the body correctly.
4. `call.toCurl()` returns a copyable curl command without executing.
5. `call.execute()` triggers the network call.
6. Error responses (status >= 400) throw `HttpException` with full context.
7. Transport errors throw `HttpException` with root cause.
8. Retry works for configured status codes and methods.
9. Retry does not happen for POST by default.
10. Timeout triggers `HttpException` with appropriate context.
11. Upload streams the file from disk, does not buffer in memory.
12. Download writes directly to the target file, does not buffer in memory.
13. Stream returns an `InputStream` that the caller controls.
14. Multipart encodes fields and files correctly per RFC 7578.
15. Zero production dependencies in the core library.
16. All tests pass without internet access.
17. All tests are deterministic.
18. Java 17 compatibility (no preview features, no external dependencies).

## Implementation Roadmap

See [docs/IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the detailed step-by-step plan.

### Summary

| Phase | Focus |
|---|---|
| Phase 1 | Public API classes/records, text request, HttpResult, HttpException, toCurl |
| Phase 2 | Config, timeout, retry, attempts tracking, concurrency limit |
| Phase 3 | Upload, download, stream |
| Phase 4 | Multipart/form-data |
| Phase 5 | Harden tests, finalize docs, prepare for release |

## Review Checklist

See [docs/REVIEW_CHECKLIST.md](REVIEW_CHECKLIST.md) for the implementation, hardening, and release review checklist.
