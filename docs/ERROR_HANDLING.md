# Error Handling

KissRequests throws a single exception type for all execution failures: `HttpException`.

## Philosophy

Errors should be actionable. When a request fails, you need to know:
- What was sent (method, URL, headers, body).
- What was attempted (retry history).
- How long it took.
- What went wrong (status code, response body, root cause).

All of this is captured in `HttpException`.

## HttpException

Extends `RuntimeException`. All execution failures throw this exception.

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
}
```

## Basic Usage

```java
try {
    HttpResult result = http.request(HttpMethod.GET, "https://api.example.com/users/999").execute();
} catch (HttpException e) {
    System.err.println("Failed: " + e.getMessage());
    // Failed: HTTP 404 for GET https://api.example.com/users/999 (1 attempt, 234ms)
}
```

## Error Report

The `report()` method returns a human-readable multi-line report containing all available debug information:

```java
try {
    http.request(HttpMethod.GET, "https://api.example.com/users/999").execute();
} catch (HttpException e) {
    System.err.println(e.report());
    // Output:
    // HTTP 404 for GET https://api.example.com/users/999 (1 attempt, 234ms)
    //   Attempt 1: status=404, duration=234ms - HTTP 404
    //   Curl: curl 'https://api.example.com/users/999'
    //   Response: {"error": "Not found"}
}
```

## Error Categories

### HTTP Error (status >= 400)

```java
try {
    http.request(HttpMethod.GET, "https://api.example.com/missing").execute();
} catch (HttpException e) {
    System.out.println(e.statusCode());     // 404
    System.out.println(e.responseHeaders()); // response headers, if available
    System.out.println(e.responseBody());   // {"error": "Not found"}
    System.out.println(e.rootCause());      // null
}
```

### Timeout

```java
try {
    http.request(HttpMethod.GET, "https://api.example.com/slow").execute();
} catch (HttpException e) {
    System.out.println(e.statusCode());     // -1
    System.out.println(e.rootCause());      // HttpTimeoutException
    // Message includes the method, URL, cause message, attempts, and duration.
}
```

### Transport Error (connection refused, DNS failure, etc.)

```java
try {
    http.request(HttpMethod.GET, "https://nonexistent.host.example.com/api").execute();
} catch (HttpException e) {
    System.out.println(e.statusCode());     // -1
    System.out.println(e.rootCause());      // IOException
    // Message includes the method, URL, cause message, attempts, and duration.
}
```

### Unexpected Error

```java
try {
    http.request(HttpMethod.GET, "https://api.example.com/users").execute();
} catch (HttpException e) {
    if (e.rootCause() != null) {
        e.rootCause().printStackTrace();
    }
}
```

## Inspecting Retry Attempts

When retry is configured, you can inspect all attempts:

```java
Http http = Http.builder()
        .retryPolicy(RetryPolicy.of(3, Duration.ofMillis(500)))
        .build();

try {
    http.request(HttpMethod.GET, "https://api.example.com/flaky").execute();
} catch (HttpException e) {
    System.out.println("Total attempts: " + e.attempts().size());
    for (HttpAttempt attempt : e.attempts()) {
        System.out.printf("  Attempt %d: status=%d, duration=%dms, failure=%s%n",
                attempt.attemptNumber(),
                attempt.statusCode(),
                attempt.duration().toMillis(),
                attempt.failureMessage());
    }
    System.out.println("Total duration: " + e.totalDuration().toMillis() + "ms");
    System.out.println("Curl: " + e.curl());
}
```

## Response Body Truncation

Response bodies in exceptions are truncated to 4KB to prevent memory issues:

```java
try {
    http.request(HttpMethod.GET, "https://api.example.com/large-error").execute();
} catch (HttpException e) {
    String body = e.responseBody();
    // body is truncated to 4096 bytes if the response was larger
    // ends with "\n... [truncated]" if truncated
}
```

## Interrupted Threads

If the thread is interrupted during execution, the interrupt flag is restored:

```java
Thread t = new Thread(() -> {
    try {
        http.request(HttpMethod.GET, "https://api.example.com/slow").execute();
    } catch (HttpException e) {
        System.out.println(Thread.currentThread().isInterrupted()); // true
    }
});
t.start();
t.interrupt();
```

Use `HttpMethod` constants for standard methods. Raw strings are accepted for custom or uncommon methods.
