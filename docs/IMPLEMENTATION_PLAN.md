# Implementation Plan

This document provides the step-by-step implementation plan for KissRequests.

**Implementation status: Complete.** All phases have been implemented. 84 tests pass.

Each phase includes production code, tests, and documentation updates.

## Phase 1: Core Text Request -- COMPLETE

**Goal:** Make a simple GET/POST work end-to-end.

### Tasks

1. Create `HttpMethod` constants class.
2. Create `HttpResult` record (statusCode, headers, body, duration, attempts, method, url).
3. Create `HttpException` class with all fields.
4. Create `HttpAttempt` record (attemptNumber, statusCode, duration, failureMessage).
5. Create `HttpConfig` record.
6. Create `CurlRenderer` internal utility.
7. Create generic `HttpCall<T>` class (prepare + execute + toCurl).
8. Create `HttpExecutionEngine` internal class.
9. Create `Http` facade with `request(...)` method.
10. Implement text request execution using JDK `HttpClient`.

### Tests

- Local HTTP test server using `com.sun.net.httpserver.HttpServer`.
- GET request test.
- POST request with body test.
- PUT, DELETE, PATCH, HEAD, OPTIONS tests.
- Request with headers test.
- `toCurl()` tests for GET and POST.
- Error response test (status >= 400 throws HttpException).
- Transport error test (connection refused).
- Invalid input tests (null URL, empty method, etc.).

### Documentation

- Update API.md with implemented signatures.
- Update EXAMPLES.md with working examples.
- Update GETTING_STARTED.md with confirmed API.

## Phase 2: Configuration, Retry, Timeout, Concurrency -- COMPLETE

**Goal:** Add configurable retry, timeout, and concurrency.

### Tasks

1. Implement `RetryPolicy` immutable class.
2. Implement retry logic in `HttpExecutionEngine`.
   - Exponential backoff with jitter.
   - Retry on configured status codes and methods.
   - Track all attempts.
3. Implement timeout application.
   - Connect timeout on `HttpClient.Builder`.
   - Request timeout on `HttpRequest.Builder`.
4. Implement `ConcurrencyLimiter` internal class.
   - Semaphore-based.
   - Acquired before request, released in `finally`.
5. Implement `Http.builder()` with all configuration options.
6. Implement timeout error handling (HttpTimeoutException).
7. Implement interrupt handling (restore interrupt flag).

### Tests

- Retry on 500 test.
- Retry on 502, 503, 504 test.
- No retry on 404 test.
- No retry on POST by default test.
- Retry respects maxAttempts test.
- Retry with backoff timing test (approximate).
- Connect timeout test.
- Request timeout test.
- Concurrency limit test (multiple concurrent requests).
- Interrupt handling test.
- Attempts tracking in exception test.

### Documentation

- Update CONFIGURATION.md with confirmed options.
- Update ERROR_HANDLING.md with retry examples.
- Update EXAMPLES.md with configuration examples.

## Phase 3: Upload, Download, Stream -- COMPLETE

**Goal:** Support file operations and streaming.

### Tasks

1. Implement `http.upload(...)` using `BodyPublishers.ofFile()`.
2. Implement `http.download(...)` by streaming the response directly to the target file.
3. Create `HttpDownloadResult` record (statusCode, headers, file, bytesWritten, duration, attempts, method, url).
4. Implement `http.stream(...)` using `BodyHandlers.ofInputStream()`.
5. Create `HttpStreamResult` record (statusCode, headers, inputStream, duration, attempts, method, url).
6. Extend `CurlRenderer` for upload, download, stream.
7. Add file validation (exists, readable) before upload.
8. Handle large files without memory buffering.
9. Create parent directories for download target if they do not exist.

### Tests

- Upload small file test.
- Upload with custom Content-Type test.
- Upload missing file test (expect exception).
- Download to file test.
- Download bytesWritten verification test.
- Download to missing directory test (auto-creates parent directories).
- Stream response test.
- Stream close test.
- Large file upload/download test (verify no OOM).
- toCurl for upload/download/stream tests.

### Documentation

- Update FILE_UPLOAD_DOWNLOAD.md with confirmed API.
- Update EXAMPLES.md with file operation examples.

## Phase 4: Multipart/form-data -- COMPLETE

**Goal:** Support multipart form uploads.

### Tasks

1. Implement multipart body builder.
   - Generate boundary string.
   - Encode text fields.
   - Stream file parts from disk.
   - Use `BodyPublishers.ofByteArrays()` or similar streaming approach.
2. Implement `http.multipart(...)`.
3. Set `Content-Type: multipart/form-data; boundary=...` header automatically.
4. Extend `CurlRenderer` for multipart (`-F` flags).

### Tests

- Multipart with text fields only test.
- Multipart with files only test.
- Multipart with both text and files test.
- Multipart content-type header verification test.
- Multipart with missing file test (expect exception).
- Multipart toCurl test.

### Documentation

- Update FILE_UPLOAD_DOWNLOAD.md with multipart examples.
- Update EXAMPLES.md with multipart example.

## Phase 5: Harden Tests and Documentation -- COMPLETE

**Goal:** Prepare for release.

### Tasks

1. Review all tests for completeness and clarity.
2. Add any missing edge case tests.
3. Verify all public methods have at least one test.
4. Review all documentation for accuracy and consistency.
5. Verify README.md matches current API.
6. Verify docs/index.md links to all pages.
7. Verify EXAMPLES.md examples are copyable and correct.
8. Run full test suite and verify all tests pass.
9. Verify `mvn -B verify` passes clean.
10. Update CHANGELOG.md for the initial release.
11. Remove all placeholder and design/specification-phase markers in docs.

### Tests

- Full regression test suite (84 tests passing).
- Performance sanity test (not a benchmark, just no pathological behavior).

### Documentation

- Final review of all documentation.
- Verify GitHub Pages builds correctly.
- Verify Maven Central readiness.

## Execution Order

Phases were executed in order. Each phase built on the previous one.

Within each phase:
1. Write production code.
2. Write tests.
3. Run tests.
4. Update documentation.
5. Verify everything passes.

All phases are complete. The library is ready for v0.1.0 release.
