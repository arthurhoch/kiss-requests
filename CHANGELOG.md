# Changelog

All notable changes to KissRequests will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2026-04-29

### Added

- `Http` facade with `create()`, `builder()`, and `request()`, `upload()`, `download()`, `stream()`, `multipart()` methods.
- `HttpCall<T>` prepared call with `execute()` and `toCurl()`.
- `HttpResult` record (statusCode, headers, body, duration, attempts, method, url).
- `HttpDownloadResult` record (statusCode, headers, file, bytesWritten, duration, attempts, method, url).
- `HttpStreamResult` record (statusCode, headers, inputStream, duration, attempts, method, url).
- `HttpException` rich exception with method, url, curl, attempts, totalDuration, statusCode, responseHeaders, responseBody, rootCause, report().
- `HttpAttempt` record (attemptNumber, statusCode, duration, failureMessage).
- `HttpMethod` constants (GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS).
- `RetryPolicy` immutable class with `defaults()`, `of(maxAttempts)`, `of(maxAttempts, backoff)`, and custom status/method retry configuration.
- `HttpConfig` record (connectTimeout, requestTimeout, retryPolicy, maxConcurrentRequests, executor).
- `Http.builder()` with connectTimeout, requestTimeout, retryPolicy, maxConcurrentRequests, executor options.
- Retry with exponential backoff and jitter, capped at 30 seconds.
- Retry on status codes 429, 500, 502, 503, 504 by default.
- Retry only on idempotent methods (GET, HEAD, OPTIONS, PUT, DELETE) by default.
- Validation for retry policy, timeout, and concurrency configuration.
- Concurrency limiting via Semaphore.
- Response body truncation to 4KB in HttpException.
- Interrupt flag restoration on interrupted threads.
- Parent directory auto-creation for download targets.
- Automatic Content-Type header with boundary for multipart requests.
- File streaming from disk for upload and multipart (no full-memory buffering).
- Curl rendering for all request types via `toCurl()`.
- Unit tests using local `com.sun.net.httpserver.HttpServer` (no internet required).
- Maven project with Java 17 compatibility.
- Zero external dependencies.
- GitHub Actions CI, Pages, and release workflows.
- Full documentation: README, API reference, examples, error handling, configuration, file operations.

### Changed

- Updated all documentation from design/specification phase to reflect implemented API.
- Removed all "design/specification phase" status markers.
