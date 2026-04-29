# 07 — Configuration Model

This document defines the singleton and configuration model for KissRequests.

## Design Principles

1. No required configuration. `Http.create()` must work with sensible defaults.
2. Configuration is immutable after construction.
3. Configuration lives on the `Http` singleton, not on individual calls.
4. Thread-safe. The same `Http` instance can be shared across threads.

## Default Instance

```java
Http http = Http.create();
```

**Defaults:**
- Connect timeout: 10 seconds
- Request timeout: 30 seconds
- Retry policy: no retries (1 attempt)
- Max concurrent requests: unlimited (0)
- Executor: default (the JDK HttpClient's default executor)
- Status policy: status >= 400 is an error

## Builder

```java
Http http = Http.builder()
        .connectTimeout(Duration.ofSeconds(5))
        .requestTimeout(Duration.ofSeconds(60))
        .retryPolicy(RetryPolicy.of(3, Duration.ofMillis(500)))
        .maxConcurrentRequests(10)
        .executor(Executors.newCachedThreadPool())
        .build();
```

### `HttpConfig` Fields

| Field | Type | Default | Description |
|---|---|---|---|
| `connectTimeout` | `Duration` | 10 seconds | Timeout for establishing the connection. Applied to `HttpClient.Builder.connectTimeout()`. |
| `requestTimeout` | `Duration` | 30 seconds | Timeout for the full request/response. Applied to `HttpRequest.Builder.timeout()`. |
| `retryPolicy` | `RetryPolicy` | 1 attempt, no retries | Retry configuration. |
| `maxConcurrentRequests` | `int` | 0 (unlimited) | Maximum concurrent requests. Uses a semaphore internally. |
| `executor` | `Executor` | null (default) | Optional executor for the HttpClient. If null, uses the JDK default. |

### `RetryPolicy` Fields

| Field | Type | Default | Description |
|---|---|---|---|
| `maxAttempts` | `int` | 1 | Maximum number of attempts (1 = no retry). |
| `initialBackoff` | `Duration` | 0ms by default, 500ms when retry is enabled with `RetryPolicy.of(maxAttempts)` | Initial backoff duration before first retry. |
| `retryOnStatusCodes` | `Set<Integer>` | empty by default, {429, 500, 502, 503, 504} when retry is enabled with `RetryPolicy.of(...)` | Status codes that trigger a retry. |
| `retryOnMethods` | `Set<String>` | empty by default, {GET, HEAD, OPTIONS, PUT, DELETE} when retry is enabled with `RetryPolicy.of(...)` | HTTP methods that are safe to retry. POST is NOT retried by default unless explicitly configured. |

### Retry Backoff Formula

Exponential backoff with jitter:

```
delay = initialBackoff * 2^(attemptNumber - 1) + random(0, initialBackoff)
```

Cap the delay at a reasonable maximum (e.g., 30 seconds).

## No Required Configuration

- `Http.create()` must return a fully usable instance.
- Every config field has a safe default.
- No config file. No system properties. No environment variables.
- The user configures only what they want to change.
- Timeouts must be positive, retry policy must be present, and max concurrency must be 0 or greater.

## Java 17 Compatibility

- No virtual threads API usage. Virtual threads are available via user-provided `Executor`.
- No preview features.
- No `java.lang.foreign` or other incubator APIs.
- Standard `java.net.http.HttpClient` only.

## Thread Safety

- `Http` is thread-safe after construction.
- `HttpConfig` and `RetryPolicy` are immutable value objects.
- The underlying `HttpClient` is thread-safe by JDK contract.
- The `ConcurrencyLimiter` uses a `Semaphore` which is thread-safe.

## Virtual Threads

- KissRequests does not require virtual threads.
- Users on Java 21+ can pass `Executors.newVirtualThreadPerTaskExecutor()` as the executor.
- The library does not detect or use virtual threads internally in v1.
- A future version may add a convenience method for virtual threads when running on Java 21+.
