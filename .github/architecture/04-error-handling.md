# 04 — Error Handling

This document defines the rich error behavior for KissRequests.

## Principles

1. All execution failures throw `HttpException`.
2. `HttpException` contains everything needed to debug the failure.
3. Errors are actionable. The user should know what happened, where, and why.
4. No vague exceptions. No "request failed" without context.
5. Interrupted threads must restore the interrupt flag.

## `HttpException`

```java
public class HttpException extends RuntimeException {
    private final String method;
    private final String url;
    private final String curl;
    private final List<HttpAttempt> attempts;
    private final Duration totalDuration;
    private final int statusCode;
    private final Map<String, List<String>> responseHeaders;
    private final String responseBody;
    private final Throwable rootCause;
}
```

### Fields

| Field | Type | Description |
|---|---|---|
| `method` | `String` | HTTP method used (for example, `HttpMethod.GET` resolves to `"GET"`). |
| `url` | `String` | Full URL of the request. |
| `curl` | `String` | Curl representation of the request (from `toCurl()`). |
| `attempts` | `List<HttpAttempt>` | All attempts made, including the final one. |
| `totalDuration` | `Duration` | Total wall-clock time across all attempts. |
| `statusCode` | `int` | HTTP status code of the last response, or -1 if no response was received. |
| `responseHeaders` | `Map<String, List<String>>` | Response headers from the last response, or empty if no response was received. |
| `responseBody` | `String` | Response body of the last response, truncated to the body limit. Null if no response body. |
| `rootCause` | `Throwable` | The underlying exception (IOException, timeout, etc.). Null if the failure was a rejected status code. |

### Response Body Limits

- Response bodies in exceptions must be truncated to a reasonable limit.
- Default limit: 4096 bytes (4KB).
- If truncated, append `\n... [truncated]` to indicate truncation.
- This prevents memory issues when servers return large error responses.
- The limit may be configurable in the future but is not a v1 public API.

### Exception Message

The exception message (`getMessage()`) must be a human-readable summary:

```
HTTP 404 for GET https://api.example.com/users/999 (3 attempts, 1523ms)
```

If no response was received:

```
HTTP request failed for GET https://api.example.com/users: Connection refused (2 attempts, 5000ms)
```

## Error Categories

### Timeout Error

- Triggered by `HttpTimeoutException`.
- Status code: -1 (no response received).
- Root cause: the `HttpTimeoutException`.
- Message follows the no-response format and includes method, URL, root cause message, attempts, and duration.

### Transport Error

- Triggered by `IOException` (connection refused, DNS failure, SSL error, etc.).
- Status code: -1 (no response received).
- Root cause: the `IOException`.
- Message follows the no-response format and includes method, URL, root cause message, attempts, and duration.

### Rejected Status Code

- Triggered when the response status code is >= 400.
- Status code: the actual response status code.
- Response body: included (truncated).
- Root cause: null (the request reached the server, but the response was rejected).
- Message: "HTTP {statusCode} for {method} {url} ({attempts} attempts, {duration}ms)".

### Invalid Request / Unexpected Execution Error

- Triggered by invalid URLs, interruption, or other execution failures caught by the engine.
- Root cause: the unexpected throwable.
- Message follows the no-response format and includes method, URL, root cause message, attempts, and duration.

## Retry Attempts Visibility

- `HttpException.attempts()` returns the full list of attempts.
- Each `HttpAttempt` contains: attempt number, status code, duration, failure message.
- This allows users to inspect the retry history for debugging.

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

## Usage in try/catch

Errors must be useful inside try/catch:

```java
try {
    HttpResult result = http.request(HttpMethod.POST, "https://api.example.com/users",
            Map.of("Content-Type", "application/json"),
            "{\"name\":\"Arthur\"}")
            .execute();
    System.out.println(result.body());
} catch (HttpException e) {
    System.err.println("Request failed: " + e.getMessage());
    System.err.println("Status: " + e.statusCode());
    System.err.println("Response: " + e.responseBody());
    System.err.println("Curl: " + e.curl());
    System.err.println("Attempts: " + e.attempts().size());
    System.err.println("Duration: " + e.totalDuration().toMillis() + "ms");
    if (e.rootCause() != null) {
        e.rootCause().printStackTrace();
    }
}
```

## Interrupted Thread Handling

1. If the thread is interrupted during execution (e.g., `InterruptedException` is caught internally):
   - Restore the interrupt flag: `Thread.currentThread().interrupt()`.
   - Throw `HttpException` with the interruption information.
   - Include the root cause as the `InterruptedException`.
2. This ensures that interrupt-based cancellation works correctly.
3. The interrupt flag must always be restored, even if the exception is wrapped.

## Concurrency Limit Exceeded

- If the concurrency semaphore is acquired but the thread is interrupted while waiting:
  - Restore the interrupt flag.
  - Throw `HttpException` with a message indicating concurrency limit exceeded or interrupted.
- If the concurrency limit is a hard limit and cannot be acquired within a reasonable time:
  - This is a design choice. The simplest approach is to block on `Semaphore.acquire()` and let the interrupt handle cancellation. Do not add a timeout to the semaphore acquire in v1.
