# API Reference

Package: `io.github.arthurhoch.kissrequests`

## `Http`

The main entry point. Thread-safe singleton facade that holds configuration and creates prepared calls.

### `Http.create()`

Returns a default instance with sensible defaults.

```java
Http http = Http.create();
```

### `Http.builder()`

Returns a builder for advanced configuration.

```java
Http http = Http.builder()
        .connectTimeout(Duration.ofSeconds(5))
        .requestTimeout(Duration.ofSeconds(30))
        .retryPolicy(RetryPolicy.of(3))
        .maxConcurrentRequests(10)
        .executor(Executors.newCachedThreadPool())
        .build();
```

Builder methods:

| Method | Description |
|---|---|
| `connectTimeout(Duration timeout)` | Sets the TCP connection timeout. |
| `requestTimeout(Duration timeout)` | Sets the per-request timeout. |
| `retryPolicy(RetryPolicy policy)` | Sets retry behavior. |
| `maxConcurrentRequests(int max)` | Sets the concurrency limit. `0` means unlimited. |
| `executor(Executor executor)` | Sets the optional executor for the underlying `HttpClient`. |
| `build()` | Creates the configured `Http` instance. |

### `http.request(...)`

Creates a prepared `HttpCall<HttpResult>` for a text request.

```java
HttpCall<HttpResult> request(String method, String url);
HttpCall<HttpResult> request(String method, String url, Map<String, String> headers);
HttpCall<HttpResult> request(String method, String url, Map<String, String> headers, String body);
```

### `http.upload(...)`

Creates a prepared `HttpCall<HttpResult>` for a file upload. Streams from disk.

```java
HttpCall<HttpResult> upload(String method, String url, Map<String, String> headers, Path file);
```

### `http.download(...)`

Creates a prepared `HttpCall<HttpDownloadResult>` for downloading to a file. Creates parent directories if they do not exist.

```java
HttpCall<HttpDownloadResult> download(String method, String url, Map<String, String> headers, Path targetPath);
```

### `http.stream(...)`

Creates a prepared `HttpCall<HttpStreamResult>` for streaming the response.

```java
HttpCall<HttpStreamResult> stream(String method, String url, Map<String, String> headers, String body);
```

### `http.multipart(...)`

Creates a prepared `HttpCall<HttpResult>` for a multipart/form-data request. Sets the `Content-Type` header automatically with boundary. Streams files from disk.

```java
HttpCall<HttpResult> multipart(String method, String url, Map<String, String> headers,
                               Map<String, String> fields, Map<String, Path> files);
```

## `HttpCall<T>`

A prepared executable call. Immutable. Does not execute until `execute()` is called.

### `call.execute()`

Executes the network call. Returns the prepared call's result type. Throws `HttpException` on failure.

```java
// For request() calls:
HttpCall<HttpResult> call = http.request(HttpMethod.GET, "https://api.example.com/users");
HttpResult result = call.execute();

// For download() calls:
HttpCall<HttpDownloadResult> call = http.download(HttpMethod.GET, "https://api.example.com/files/123",
        Map.of(), Path.of("downloaded.pdf"));
HttpDownloadResult result = call.execute();

// For stream() calls:
HttpCall<HttpStreamResult> call = http.stream(HttpMethod.GET, "https://api.example.com/stream",
        Map.of(), null);
HttpStreamResult result = call.execute();
```

### `call.toCurl()`

Returns the curl representation of the call without executing.

```java
String curl = call.toCurl();
```

### `call.toCurlBase64()`

Returns the Base64-encoded curl representation of the call without executing. Uses `java.util.Base64` with UTF-8. Useful for logs, copy/paste, or environments that distort raw text. This is a debugging convenience, not encryption or secret protection.

```java
String curlBase64 = call.toCurlBase64();
```

### Getters

| Method | Return Type | Description |
|---|---|---|
| `method()` | `String` | HTTP method. |
| `url()` | `String` | Request URL. |
| `headers()` | `Map<String, String>` | Request headers. |
| `body()` | `String` | Request body (may be null). |
| `file()` | `Path` | Upload file path (may be null). |
| `targetPath()` | `Path` | Download target path (may be null). |
| `fields()` | `Map<String, String>` | Multipart text fields. |
| `fileFields()` | `Map<String, Path>` | Multipart file fields. |
| `callType()` | `CallType` | Type of call (TEXT, UPLOAD, DOWNLOAD, STREAM, MULTIPART). |

## `HttpResult`

Java record. Result of a text request or upload.

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

## `HttpDownloadResult`

Java record. Result of a download operation.

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

## `HttpStreamResult`

Java record. Result of a stream operation.

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

## `HttpException`

Rich exception thrown on all execution failures. Extends `RuntimeException`.

```java
public class HttpException extends RuntimeException {
    public String method();
    public String url();
    public String curl();
    public List<HttpAttempt> attempts();
    public Duration totalDuration();
    public int statusCode();       // -1 if no response received
    public Map<String, List<String>> responseHeaders();
    public String responseBody();  // null if no response body, truncated to 4KB
    public Throwable rootCause();  // null if status rejection
    public Throwable getCause();   // overridden from RuntimeException
    public String report();        // human-readable multi-line report
    public static String truncateBody(String body);
}
```

`truncateBody(...)` is public for the library's own error-body limiting behavior. Most consumer code should use `responseBody()` and `report()` instead.

## `HttpAttempt`

Java record. Record of a single attempt.

```java
public record HttpAttempt(
    int attemptNumber,
    int statusCode,
    Duration duration,
    String failureMessage
) {}
```

## `HttpMethod`

Constants for standard HTTP methods. `HttpMethod` is a constants class, not an enum.

Use these constants in normal code and examples. Raw method strings are still accepted by the API for custom or uncommon methods, because request methods are passed as `String`.

| Constant | Value |
|---|---|
| `HttpMethod.GET` | `"GET"` |
| `HttpMethod.POST` | `"POST"` |
| `HttpMethod.PUT` | `"PUT"` |
| `HttpMethod.DELETE` | `"DELETE"` |
| `HttpMethod.PATCH` | `"PATCH"` |
| `HttpMethod.HEAD` | `"HEAD"` |
| `HttpMethod.OPTIONS` | `"OPTIONS"` |

## `RetryPolicy`

Configuration for retry behavior. Immutable final class.

### Factory methods

```java
RetryPolicy.defaults();                            // No retries (1 attempt)
RetryPolicy.of(int maxAttempts);                   // Custom max attempts, default backoff 500ms
RetryPolicy.of(int maxAttempts, Duration backoff); // Custom max attempts and backoff
RetryPolicy.of(int maxAttempts, Duration backoff,
        Set<Integer> statuses, Set<String> methods);
```

Default retry-on status codes: 429, 500, 502, 503, 504.  
Default retry-on methods: GET, HEAD, OPTIONS, PUT, DELETE.
`maxAttempts` must be at least 1.

### Methods

| Method | Return Type | Description |
|---|---|---|
| `maxAttempts()` | `int` | Maximum number of attempts. |
| `initialBackoff()` | `Duration` | Initial backoff duration. |
| `retryOnStatusCodes()` | `Set<Integer>` | Status codes that trigger retry. |
| `retryOnMethods()` | `Set<String>` | HTTP methods that are safe to retry. |
| `shouldRetryOnStatus(int statusCode)` | `boolean` | Whether a status code is retryable by this policy. |
| `shouldRetryMethod(String method)` | `boolean` | Whether a method is retryable by this policy. |

## `HttpConfig`

Java record. Configuration held by the `Http` singleton.

```java
public record HttpConfig(
    Duration connectTimeout,
    Duration requestTimeout,
    RetryPolicy retryPolicy,
    int maxConcurrentRequests,
    Executor executor
) {}
```

```java
HttpConfig.defaults();
```

| Field | Default | Description |
|---|---|---|
| `connectTimeout` | 10s | Connection establishment timeout. |
| `requestTimeout` | 30s | Full request/response timeout. |
| `retryPolicy` | 1 attempt | Retry configuration. |
| `maxConcurrentRequests` | 0 (unlimited) | Maximum concurrent requests. |
| `executor` | null (default) | Executor for HttpClient. |

Timeouts must be non-null and positive. `retryPolicy` must be non-null.
`maxConcurrentRequests` must be 0 or greater.
