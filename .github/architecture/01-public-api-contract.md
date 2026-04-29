# 01 — Public API Contract

This document defines the implemented public API contract for KissRequests v1.

## Entry Point: `Http`

The `Http` class is the singleton facade. It holds configuration and creates prepared calls.

```java
// Default instance with sensible defaults
Http http = Http.create();

// Configured instance via builder
Http http = Http.builder()
        .connectTimeout(Duration.ofSeconds(5))
        .requestTimeout(Duration.ofSeconds(30))
        .retryPolicy(RetryPolicy.of(3))
        .maxConcurrentRequests(10)
        .executor(Executors.newCachedThreadPool())
        .build();
```

**Contract:**
- `Http.create()` returns a usable default instance. No configuration required.
- `Http.builder()` returns a builder for advanced configuration.
- `Http` is thread-safe. A single instance can be shared across threads.
- The underlying `java.net.http.HttpClient` is reused across calls.

## Prepared Call: `HttpCall`

`http.request(...)` returns a prepared `HttpCall<HttpResult>`. It does NOT execute the network call.

```java
HttpCall<HttpResult> call = http.request(HttpMethod.GET, "https://api.example.com/users");
```

**Overloaded `request` signatures:**

```java
// Text request without headers or body
HttpCall<HttpResult> request(String method, String url);

// Text request with headers
HttpCall<HttpResult> request(String method, String url, Map<String, String> headers);

// Text request with headers and body
HttpCall<HttpResult> request(String method, String url, Map<String, String> headers, String body);
```

**`HttpCall<T>` methods:**

```java
// Execute the call and return the result
T execute() throws HttpException;

// Render the call as a curl command without executing
String toCurl();

// Render the call as a Base64-encoded curl command without executing
String toCurlBase64();
```

**Contract:**
- `request(...)` never executes the network call.
- `execute()` triggers the actual network call.
- `toCurl()` can be called before or after `execute()` without side effects.
- `HttpCall<T>` is immutable. It can be stored and executed multiple times if needed (each call creates a new network request).
- The generic type `T` is the result type returned by `execute()`.

## Specialized Operations

### Upload

```java
HttpCall<HttpResult> call = http.upload(HttpMethod.POST, "https://api.example.com/files",
        Map.of("Content-Type", "application/octet-stream"),
        Path.of("document.pdf"));
HttpResult result = call.execute();
```

### Download

```java
HttpCall<HttpDownloadResult> call = http.download(HttpMethod.GET, "https://api.example.com/files/123",
        Map.of(),
        Path.of("downloaded.pdf"));
HttpDownloadResult result = call.execute();
```

### Stream

```java
HttpCall<HttpStreamResult> call = http.stream(HttpMethod.GET, "https://api.example.com/stream",
        Map.of(),
        null);
HttpStreamResult result = call.execute();
// result.inputStream() provides the response body as InputStream
```

### Multipart

```java
HttpCall<HttpResult> call = http.multipart(HttpMethod.POST, "https://api.example.com/upload",
        Map.of("Authorization", "Bearer token"),
        Map.of("name", "Arthur", "email", "arthur@example.com"),
        Map.of("avatar", Path.of("photo.jpg"), "resume", Path.of("cv.pdf")));
HttpResult result = call.execute();
```

## Result Types

### `HttpResult`

```java
public record HttpResult(
        int statusCode,
        Map<String, List<String>> headers,
        String body,
        Duration duration,
        List<HttpAttempt> attempts,
        String method,
        String url
) {}
```

### `HttpDownloadResult`

```java
public record HttpDownloadResult(
        int statusCode,
        Map<String, List<String>> headers,
        Path file,
        long bytesWritten,
        Duration duration,
        List<HttpAttempt> attempts,
        String method,
        String url
) {}
```

### `HttpStreamResult`

```java
public record HttpStreamResult(
        int statusCode,
        Map<String, List<String>> headers,
        InputStream inputStream,
        Duration duration,
        List<HttpAttempt> attempts,
        String method,
        String url
) {}
```

**Contract:**
- `HttpStreamResult.inputStream()` must be closed by the caller.
- The stream is not buffered by the library. The caller is responsible for reading.

## `HttpMethod` Constants

```java
public final class HttpMethod {
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    public static final String PATCH = "PATCH";
    public static final String HEAD = "HEAD";
    public static final String OPTIONS = "OPTIONS";
}
```

**Contract:**
- `HttpMethod` provides String constants for standard HTTP methods.
- Methods are passed as strings to keep the API simple. An enum would add unnecessary complexity.
- User-facing examples should prefer `HttpMethod.GET`, `HttpMethod.POST`, and the other constants for standard methods.
- Users can also pass raw strings for custom or uncommon methods (for example, `"PROPFIND"`).

## `HttpException`

```java
public class HttpException extends RuntimeException {
    public String method();
    public String url();
    public String curl();
    public List<HttpAttempt> attempts();
    public Duration totalDuration();
    public int statusCode();       // -1 if not available
    public Map<String, List<String>> responseHeaders();
    public String responseBody();  // null if not available, truncated if too large
    public Throwable rootCause();
    public Throwable getCause();
    public String report();
}
```

**Contract:**
- All execution failures throw `HttpException`.
- Contains all available debugging information.
- Response body in exception is truncated to a reasonable limit (e.g., 4KB) to avoid memory/log issues.

## `HttpAttempt`

```java
public record HttpAttempt(
        int attemptNumber,
        int statusCode,
        Duration duration,
        String failureMessage
) {}
```

## `RetryPolicy`

```java
public final class RetryPolicy {
    public static RetryPolicy defaults();
    public static RetryPolicy of(int maxAttempts);
    public static RetryPolicy of(int maxAttempts, Duration initialBackoff);
    public static RetryPolicy of(int maxAttempts, Duration initialBackoff,
                                 Set<Integer> retryOnStatusCodes,
                                 Set<String> retryOnMethods);
    public int maxAttempts();
    public Duration initialBackoff();
    public Set<Integer> retryOnStatusCodes();
    public Set<String> retryOnMethods();
}
```

**Contract:**
- Default: no retries (1 attempt).
- Backoff: exponential with jitter.
- Retries only on configured status codes (e.g., 429, 500, 502, 503, 504).
- Retries only on configured methods (by default: GET, HEAD, OPTIONS, PUT, DELETE — not POST by default).

## `HttpConfig`

```java
public record HttpConfig(
        Duration connectTimeout,
        Duration requestTimeout,
        RetryPolicy retryPolicy,
        int maxConcurrentRequests,
        Executor executor
) {}
```

**Contract:**
- All settings have safe defaults.
- `maxConcurrentRequests()`: 0 means unlimited.
- `executor()`: null means use the default HttpClient executor.

## Status Policy

Any status >= 400 is considered an error and triggers `HttpException`. This is not configurable in v1.
