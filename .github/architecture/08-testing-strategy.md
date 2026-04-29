# 08 — Testing Strategy

This document defines the testing strategy for KissRequests.

## Principles

1. All tests use JUnit 5.
2. Tests must not require internet access.
3. Tests must not require external services.
4. Tests must be deterministic.
5. Every public method must have at least one test.
6. Test failures must produce clear messages.

## Local HTTP Test Server

All tests run against a local HTTP server started in the test JVM.

### Implementation

- Use `com.sun.net.httpserver.HttpServer` (built into the JDK) for the test server.
- Start the server on a random available port (`localhost:0` or `InetSocketAddress(0)`).
- Register handlers for each test scenario (echo, error, slow response, etc.).
- Start the server before tests, stop after.

### Test Server Handlers

| Path | Behavior |
|---|---|
| `/echo` | Returns the request method, headers, and body in the response. |
| `/status/{code}` | Returns the given status code. |
| `/slow` | Delays the response for timeout testing. |
| `/download` | Returns a known byte array for download testing. |
| `/multipart` | Accepts multipart/form-data and echoes the fields and files. |

## Test Categories

### Text Request Tests

- GET request with no body.
- POST request with body.
- PUT request with body.
- DELETE request.
- PATCH request.
- HEAD request (no response body).
- OPTIONS request.
- Request with custom headers.
- Request with no headers.
- Verify response status code.
- Verify response headers.
- Verify response body.

### Upload Tests

- Upload a small binary file.
- Upload a file with custom Content-Type.
- Upload to a URL that returns an error status.
- Upload a file that does not exist (expect exception).

### Download Tests

- Download a response to a file.
- Verify the downloaded file content matches the response.
- Verify bytesWritten is correct.
- Download to a directory that does not exist (parent directories are created).
- Download a large response (verify streaming, no OOM).

### Stream Tests

- Stream a response as InputStream.
- Read the stream fully and verify content.
- Close the stream before reading fully.
- Stream a large response without buffering.

### Multipart Tests

- Send multipart with text fields only.
- Send multipart with file fields only.
- Send multipart with both text and file fields.
- Verify the request content-type header includes boundary.
- Verify the server received all fields and files.
- Send multipart with a missing file (expect exception).

### toCurl Tests

- Render curl for GET request.
- Render curl for POST request with body.
- Render curl for request with multiple headers.
- Render curl for upload.
- Render curl for download.
- Render curl for multipart.
- Verify single-quote escaping.
- Verify URL is included.
- Verify method is included for non-GET.

### Retry Tests

- Retry on 500 status code.
- Retry on 502, 503, 504 status codes.
- Retry on 429 status code.
- Do not retry on 404.
- Do not retry POST by default.
- Verify retry respects maxAttempts.
- Verify backoff timing (approximate, with tolerance).
- Verify attempts are tracked in the exception.

### Timeout Tests

- Request that exceeds connect timeout throws HttpException.
- Request that exceeds request timeout throws HttpException.
- Verify timeout duration is included in exception.
- Verify the exception indicates timeout.

### Rich Exception Tests

- Exception contains method, URL, curl.
- Exception contains status code when available.
- Exception contains response body when available.
- Exception contains root cause.
- Exception contains attempts list.
- Exception contains total duration.
- Response body is truncated when exceeding the limit.
- Exception message is human-readable.

### Concurrency Limit Tests

- Execute multiple concurrent requests within the limit.
- Verify that requests exceeding the limit wait (using a semaphore).
- Verify that the limit is enforced correctly.

### Invalid Input Tests

- Null URL throws NullPointerException or IllegalArgumentException.
- Empty URL throws IllegalArgumentException.
- Invalid URL format (e.g., "not-a-url") throws HttpException.
- Null method throws NullPointerException.
- Empty method throws IllegalArgumentException.
- Null headers (when Map is expected): treat as empty map.
- Null body (when String is expected): treat as no body.

## Test Naming Convention

- Test classes: `{ClassName}Test` (e.g., `HttpTest`, `HttpCallTest`, `HttpExceptionTest`).
- Test methods: descriptive names using `@DisplayName` or method name describing the scenario.

## Test Structure

```java
class HttpTest {
    static HttpServer server;
    static int port;

    @BeforeAll
    static void startServer() {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/echo", exchange -> {
            // echo handler
        });
        server.start();
        port = server.getAddress().getPort();
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @Test
    void shouldExecuteGetRequest() {
        Http http = Http.create();
        HttpResult result = http.request(HttpMethod.GET, "http://localhost:" + port + "/echo").execute();
        assertEquals(200, result.statusCode());
    }
}
```

## Coverage Target

- All public methods: 100% test coverage.
- Internal classes: covered indirectly through public API tests.
- Edge cases: at least one test per documented edge case.
