# 05 — Curl Rendering

This document defines the `.toCurl()` behavior for KissRequests.

## Core Requirement

Every prepared `HttpCall` must support `toCurl()`.

Calling `toCurl()` must not execute the request. It must not modify the state of the call. It must be safe to call multiple times.

## Output Format

The output must be a copyable curl command that can be pasted into a terminal.

### Text Request

```java
HttpCall<HttpResult> call = http.request(HttpMethod.POST, "https://api.example.com/users",
        Map.of("Content-Type", "application/json", "Authorization", "Bearer secret"),
        "{\"name\":\"Arthur\"}");
```

Renders as:

```
curl -X POST 'https://api.example.com/users' -H 'Content-Type: application/json' -H 'Authorization: Bearer secret' --data-raw '{"name":"Arthur"}'
```

### GET Request (no body)

```java
HttpCall<HttpResult> call = http.request(HttpMethod.GET, "https://api.example.com/users");
```

Renders as:

```
curl 'https://api.example.com/users'
```

Note: `-X GET` is omitted because curl defaults to GET when no `-X` is specified and no request body flag is present. However, for clarity and consistency, always include `-X {METHOD}` for non-GET methods. For GET, omit `-X`.

### Upload

```java
HttpCall<HttpResult> call = http.upload(HttpMethod.POST, "https://api.example.com/files",
        Map.of("Content-Type", "application/octet-stream"),
        Path.of("document.pdf"));
```

Renders as:

```
curl -X POST 'https://api.example.com/files' -H 'Content-Type: application/octet-stream' --data-binary '@document.pdf'
```

Note: The file path is rendered as a relative path in the curl command. The actual path may be absolute. Use the path as provided.

### Download

```java
HttpCall<HttpDownloadResult> call = http.download(HttpMethod.GET, "https://api.example.com/files/123",
        Map.of(),
        Path.of("downloaded.pdf"));
```

Renders as:

```
curl 'https://api.example.com/files/123' -o 'downloaded.pdf'
```

### Multipart

```java
HttpCall<HttpResult> call = http.multipart(HttpMethod.POST, "https://api.example.com/upload",
        Map.of("Authorization", "Bearer token"),
        Map.of("name", "Arthur", "email", "arthur@example.com"),
        Map.of("avatar", Path.of("photo.jpg")));
```

Renders as:

```
curl -X POST 'https://api.example.com/upload' -H 'Authorization: Bearer token' -F 'name=Arthur' -F 'email=arthur@example.com' -F 'avatar=@photo.jpg'
```

### Stream

```java
HttpCall<HttpStreamResult> call = http.stream(HttpMethod.GET, "https://api.example.com/stream", Map.of(), null);
```

Renders as:

```
curl 'https://api.example.com/stream'
```

Stream requests render the same as regular GET requests since the curl output does not capture the streaming behavior.

## Shell Escaping

1. URLs are wrapped in single quotes: `'https://api.example.com/users'`.
2. Header values are wrapped in single quotes: `'Content-Type: application/json'`.
3. Body strings are wrapped in single quotes.
4. Single quotes within values are escaped as `'\''`.
5. This is sufficient for common cases. Complex escaping (e.g., binary data) is documented as a limitation.

## Limitations

1. Binary body content cannot be accurately represented in a curl command string. For text bodies, the raw body string is included. For file uploads, `@path` syntax is used.
2. Multiline body strings are rendered inline. Newlines in the body are preserved as-is within single quotes.
3. Unicode characters in URLs and headers are passed through as-is. The curl command may need to be run in a terminal that supports the encoding.
4. File paths in curl output use the path as provided. If the path is absolute, the curl command will only work on the same machine or with the same directory structure.
5. `toCurl()` does not read file contents. It cannot validate that the file exists or is readable.

## Base64 Output

`toCurlBase64()` returns the same curl command as `toCurl()`, encoded with `java.util.Base64` using UTF-8.

Implementation:

```java
Base64.getEncoder().encodeToString(toCurl().getBytes(StandardCharsets.UTF_8))
```

Contract:
- Does not execute the request.
- Works for all call types: text, upload, download, stream, multipart.
- This is a debugging convenience for logs, copy/paste, and environments that distort raw text.
- **Not encryption. Not secret protection.** Base64 is a trivially reversible encoding.
- Since v1 has no secret masking, sensitive data (headers, bodies) is present in the encoded output.

## v1 Exclusions

1. No secret masking. Headers and bodies are rendered as-is, including `Authorization` headers. This is by design — the purpose is debugging, and masking would reduce usefulness.
2. No credential management.
3. No output format options (e.g., JSON, verbose). The output is a single-line curl command.

## Thread Safety

`toCurl()` must be thread-safe. It must not modify any shared state.

Use `HttpMethod` constants in standard examples. Raw method strings remain accepted for custom or uncommon methods.
