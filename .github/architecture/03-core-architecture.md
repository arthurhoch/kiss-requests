# 03 — Core Architecture

This document describes the internal design of KissRequests.

## Component Overview

```
┌─────────────────────────────────────────────┐
│  Http (singleton facade)                     │
│  - create()                                  │
│  - builder()                                 │
│  - request(...)  → HttpCall<HttpResult>      │
│  - upload(...)   → HttpCall<HttpResult>      │
│  - download(...) → HttpCall<HttpDownloadResult> │
│  - stream(...)   → HttpCall<HttpStreamResult> │
│  - multipart(...→ HttpCall<HttpResult>       │
└──────────┬──────────────────────────────────┘
           │ creates
           ▼
┌─────────────────────────────────────────────┐
│  HttpCall<T> (prepared executable call)       │
│  - execute()  → T                             │
│  - toCurl()   → String                        │
│  - toCurlBase64() → String                    │
└──────────┬──────────────────────────────────┘
           │ delegates execution to
           ▼
┌─────────────────────────────────────────────┐
│  HttpExecutionEngine (internal)               │
│  - holds HttpClient                           │
│  - handles retry logic                        │
│  - handles concurrency limit                  │
│  - tracks attempts                            │
│  - creates HttpException on failure           │
└──────────┬──────────────────────────────────┘
           │ uses
           ▼
┌─────────────────────────────────────────────┐
│  java.net.http.HttpClient (JDK)               │
└─────────────────────────────────────────────┘
```

## Component Responsibilities

### `Http` (public, singleton facade)

- Holds `HttpConfig` (immutable).
- Holds the shared `java.net.http.HttpClient` instance.
- Factory methods: `request`, `upload`, `download`, `stream`, `multipart`.
- Each factory method returns a `HttpCall<T>`, never executes.
- Thread-safe. The same instance is used across all threads.
- `Http.create()` builds with defaults. `Http.builder()` allows customization.

### `HttpCall<T>` (public, immutable prepared call)

- Captures: method, URL, headers, body (or file/path/multipart parts).
- Holds a reference to the `HttpExecutionEngine` and the call type.
- `execute()` delegates to the engine and returns the prepared call's result type.
- `toCurl()` renders the call as a curl command using `CurlRenderer`.
- `toCurlBase64()` returns the same curl command encoded as Base64 for debugging logs/copy-paste.
- Immutable. Can be called multiple times (each `execute()` is a new network request).

### `HttpExecutionEngine` (internal)

- Holds the `java.net.http.HttpClient` instance.
- Executes `HttpRequest` objects.
- Applies retry policy: retries on configured status codes with exponential backoff and jitter.
- Applies concurrency limit: uses a `Semaphore` to limit concurrent requests if configured.
- Tracks attempts in a list of `HttpAttempt` records.
- Wraps failures in `HttpException` with full debugging information.
- Handles timeout: applies connect timeout to the `HttpClient` builder, request timeout to each `HttpRequest`.
- Restores interrupt flag if the thread is interrupted during execution.

### `CurlRenderer` (internal)

- Renders a prepared call as a curl command string.
- Handles text body, file paths (upload), download targets, multipart fields and files.
- Shell-escapes arguments for common cases (single quotes, special characters).
- Does not add secret masking in v1.
- Does not read file contents for body rendering — uses `@file` syntax for uploads.

### `RetryPolicy` (public, immutable value)

- Defines: max attempts, initial backoff, retry status codes, retry methods.
- Default: 1 attempt (no retry).
- Backoff: exponential with jitter. Formula: `base * 2^(attempt-1) + random jitter`.

### `StatusPolicy` (internal concept)

- Determines which status codes are considered errors.
- Default: status >= 400 is an error.
- Used by the engine to decide whether to retry or throw.
- Not a public v1 API.

### `ConcurrencyLimiter` (internal)

- Wraps a `Semaphore` with the configured max concurrent requests.
- If max is 0 or not configured, no limiting occurs.
- Acquired before each request, released after (in a `finally` block).

### `HttpResult` (public, immutable result)

- Holds: status code, response headers, body as String, duration, attempts, method, URL.
- Created by the engine after successful execution.

### `HttpDownloadResult` (public, immutable result)

- Holds: status code, response headers, file Path, bytes written, duration, attempts, method, URL.
- The engine streams the response body directly to the target file and creates parent directories when needed.

### `HttpStreamResult` (public result)

- Holds: status code, response headers, InputStream, duration, attempts, method, URL.
- The engine uses `BodyHandlers.ofInputStream()`.
- The caller is responsible for closing the stream.

### `HttpException` (public, rich exception)

- Extends `RuntimeException`.
- Contains: method, URL, curl, attempts list, total duration, status code, response headers, response body (truncated), root cause.
- Response body is truncated to 4KB to avoid memory issues.

### `HttpAttempt` (public, immutable record)

- Holds: attempt number, status code, duration, failure message.

## JDK HttpClient Reuse

- One `HttpClient` instance per `Http` facade.
- Built with `HttpClient.newBuilder()` using the configured connect timeout and executor.
- The same instance is reused for all requests from the same `Http` instance.
- The JDK handles connection pooling internally.

## Timeout Application

- **Connect timeout**: Set on `HttpClient.Builder.connectTimeout()`. Applied once during client creation.
- **Request timeout**: Set on `HttpRequest.Builder.timeout()`. Applied per request.

## Retry Application

1. Engine executes the request.
2. If the response status code is in the retry set and the method is in the retry methods set:
   - Record the attempt.
   - Wait for the backoff duration (exponential with jitter).
   - Retry up to `maxAttempts` times.
3. If an `IOException` or `HttpTimeoutException` occurs:
   - Record the attempt.
   - Retry if attempts remain.
4. If all attempts fail or the final status is an error:
   - Throw `HttpException` with all attempts and debugging info.

## Download Implementation

- Uses `HttpRequest.BodyPublishers.noBody()` or `String` body for the request.
- Uses `HttpResponse.BodyHandlers.ofInputStream()` and streams the response directly to the target file.
- The library writes directly to the file. No full buffering in memory.

## Upload Implementation

- Uses `HttpRequest.BodyPublishers.ofFile(path)` for all file uploads.
- The JDK streams file content from disk.
- The library must not load upload files fully into memory.

## Stream Implementation

- Uses `HttpResponse.BodyHandlers.ofInputStream()` for the response.
- Returns the `InputStream` to the caller via `HttpStreamResult`.
- The caller is responsible for closing the stream.
- The engine does not buffer the entire response.

## Multipart Implementation

- Builds a `multipart/form-data` request body manually.
- Uses `HttpRequest.BodyPublishers.ofInputStream(...)` with a sequence of field and file streams.
- Generates a unique boundary string.
- Encodes text fields and file parts according to the multipart/form-data specification (RFC 7578).
- Streams file content from disk. Does not load entire files into memory.

## What Must Remain Internal

- `HttpExecutionEngine`
- `CurlRenderer`
- `ConcurrencyLimiter`
- Status policy details
- Internal implementation classes live in the `internal` sub-package and are not supported user API.
- Users should only interact with public API classes.
