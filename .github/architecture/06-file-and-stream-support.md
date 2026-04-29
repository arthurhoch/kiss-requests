# 06 — File and Stream Support

This document defines file upload, download, stream, and multipart support for KissRequests.

## Binary Upload

### API

```java
HttpResult result = http.upload(HttpMethod.POST, "https://api.example.com/files",
        Map.of("Content-Type", "application/octet-stream"),
        Path.of("document.pdf")).execute();
```

### Design

- Uses `HttpRequest.BodyPublishers.ofFile(path)` for file content.
- The JDK streams the file content from disk. The library does not load the entire file into memory.
- The user provides the content type via headers. The library does not guess MIME types.
- Returns `HttpResult` with status code, headers, and response body.

### Memory Safety

- Files must be streamed from disk using `BodyPublishers.ofFile()`.
- Do not use `Files.readAllBytes()` for uploads.
- No size threshold is needed. Use `ofFile()` for all files; it handles both small and large files efficiently.

## Download to File

### API

```java
HttpDownloadResult result = http.download(HttpMethod.GET, "https://api.example.com/files/123",
        Map.of(),
        Path.of("downloaded.pdf")).execute();
System.out.println("Downloaded " + result.bytesWritten() + " bytes to " + result.file());
```

### Design

- Streams the response body directly to the target file. No full buffering in memory.
- If the target file exists, it is overwritten.
- If the target directory does not exist, the library creates parent directories before writing the file.
- Returns `HttpDownloadResult` with status code, headers, file path, and bytes written.

### Memory Safety

- The response body is streamed directly to the file.
- The library never holds the full response body in memory.
- Works for responses of any size.

### Result Type

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

## Stream Response as InputStream

### API

```java
HttpStreamResult result = http.stream(HttpMethod.GET, "https://api.example.com/stream",
        Map.of(), null).execute();
try (InputStream is = result.inputStream()) {
    is.transferTo(System.out);
}
```

### Design

- Uses `HttpResponse.BodyHandlers.ofInputStream()` for the response.
- Returns the `InputStream` to the caller via `HttpStreamResult`.
- The caller is responsible for closing the stream.
- The library does not buffer the entire response.
- Suitable for large responses or streaming APIs.

### Memory Safety

- The response body is streamed as an `InputStream`.
- The library never holds the full response body in memory.
- The caller controls how much to read and when.

### Result Type

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
- `inputStream()` must be closed by the caller to release resources.
- The underlying HTTP connection may be held open until the stream is fully read or closed.

## Multipart/form-data

### API

```java
HttpResult result = http.multipart(HttpMethod.POST, "https://api.example.com/upload",
        Map.of("Authorization", "Bearer token"),
        Map.of("name", "Arthur", "email", "arthur@example.com"),
        Map.of("avatar", Path.of("photo.jpg"), "resume", Path.of("cv.pdf"))).execute();
```

### Design

- Builds a `multipart/form-data` request body (RFC 7578).
- Generates a unique boundary string (e.g., UUID-based).
- Text fields are encoded as `Content-Disposition: form-data; name="fieldName"` with the value as the body.
- File fields are encoded as `Content-Disposition: form-data; name="fieldName"; filename="fileName"` with the file content as the body.
- The `Content-Type` header is set to `multipart/form-data; boundary=...` automatically.
- Uses `HttpRequest.BodyPublishers.ofInputStream(...)` with a streaming approach to avoid loading all files into memory at once.

### Multipart Body Structure

```
--boundary
Content-Disposition: form-data; name="name"

Arthur
--boundary
Content-Disposition: form-data; name="avatar"; filename="photo.jpg"
Content-Type: application/octet-stream

<binary content of photo.jpg>
--boundary--
```

### Memory Safety

- Text fields are small and held in memory.
- File content is streamed from disk using `InputStream` from `Files.newInputStream()`.
- The library must not load entire files into memory.
- The body publisher should read each file part sequentially, not all at once.

### Content-Type for Files

- Default: `application/octet-stream`.
- The library does not guess MIME types in v1.
- Do not set the multipart `Content-Type` header manually. If a user-provided `Content-Type` conflicts with the generated boundary, the library ignores that header and sets `multipart/form-data; boundary=...` automatically.

## Error Handling for File Operations

- If the upload file does not exist or is not readable: throw `HttpException` with a clear message indicating the file path and the error.
- If the download target directory does not exist: create parent directories before writing the file.
- If the multipart file does not exist: throw `HttpException` with a clear message.
- Do not silently skip missing files.

## Large Payload Requirements

1. Upload: stream from disk. Do not buffer.
2. Download: stream to disk. Do not buffer.
3. Stream: return `InputStream`. Caller controls reading.
4. Multipart: stream file parts from disk. Do not buffer entire multipart body in memory.
5. Response body in exceptions: truncate to 4KB.

Use `HttpMethod` constants for standard methods in examples. Raw method strings are accepted for custom or uncommon methods.
