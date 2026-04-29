# Configuration

## Default Instance

`Http.create()` returns a working instance with sensible defaults. No configuration required.

```java
Http http = Http.create();
```

### Default Values

| Setting | Default |
|---|---|
| Connect timeout | 10 seconds |
| Request timeout | 30 seconds |
| Retry policy | No retries (1 attempt) |
| Max concurrent requests | Unlimited (0) |
| Executor | JDK default |

## Builder

Use `Http.builder()` for advanced configuration:

```java
Http http = Http.builder()
        .connectTimeout(Duration.ofSeconds(5))
        .requestTimeout(Duration.ofSeconds(60))
        .retryPolicy(RetryPolicy.of(3, Duration.ofMillis(500)))
        .maxConcurrentRequests(10)
        .executor(Executors.newCachedThreadPool())
        .build();
```

## Connect Timeout

Timeout for establishing the TCP connection to the server.

```java
Http http = Http.builder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();
```

Applied to `HttpClient.Builder.connectTimeout()`. Default: 10 seconds.

## Request Timeout

Timeout for the complete request/response cycle.

```java
Http http = Http.builder()
        .requestTimeout(Duration.ofSeconds(30))
        .build();
```

Applied to `HttpRequest.Builder.timeout()` for each request. Default: 30 seconds.

## Retry Policy

Configure automatic retry on transient failures.

```java
RetryPolicy policy = RetryPolicy.of(3, Duration.ofMillis(500));

Http http = Http.builder()
        .retryPolicy(policy)
        .build();
```

### RetryPolicy Options

```java
// No retries (default)
RetryPolicy.defaults();

// Up to 3 attempts with 500ms initial backoff
RetryPolicy.of(3, Duration.ofMillis(500));

// Up to 3 attempts with default backoff (500ms)
RetryPolicy.of(3);

// Custom retry status codes and methods
RetryPolicy.of(
        3,
        Duration.ofMillis(500),
        Set.of(429, 503),
        Set.of(HttpMethod.GET, HttpMethod.POST)
);
```

### What triggers a retry

- **Status codes**: 429, 500, 502, 503, 504 by default.
- **Transport errors**: IOException (connection refused, DNS failure, etc.).
- **Methods**: Only idempotent methods are retried by default: GET, HEAD, OPTIONS, PUT, DELETE.
- **POST is NOT retried by default** because it may not be idempotent.
- Status codes and methods can be customized explicitly with the full `RetryPolicy.of(...)` factory.

### Backoff

Exponential backoff with jitter:

```
delay = initialBackoff * 2^(attempt - 1) + random(0, initialBackoff)
```

Capped at 30 seconds.

## Max Concurrent Requests

Limit the number of concurrent requests using a semaphore.

```java
Http http = Http.builder()
        .maxConcurrentRequests(10)
        .build();
```

- Default: 0 (unlimited).
- Negative values are rejected.
- Requests exceeding the limit block until a slot is available.
- Uses a `Semaphore` internally.

## Executor

Provide a custom `Executor` for the underlying `HttpClient`.

```java
// Use a fixed thread pool
Http http = Http.builder()
        .executor(Executors.newFixedThreadPool(8))
        .build();
```

- Default: null (the JDK HttpClient uses its default executor).
- Virtual threads are supported only as a user-provided `Executor` in consuming applications that run on Java 21+. KissRequests core remains Java 17-compatible and does not require virtual threads.

## Validation

- Timeouts must be non-null and positive.
- Retry policy must be non-null.
- Max concurrent requests must be 0 or greater.

## Thread Safety

- `Http` is thread-safe after construction.
- `HttpConfig` and `RetryPolicy` are immutable.
- The same `Http` instance can be shared across threads.
- Build once, use everywhere.
