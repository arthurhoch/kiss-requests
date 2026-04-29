# Curl Debugging

**Status: Implemented.**

Every prepared `HttpCall` supports `.toCurl()` for instant debugging.

## Purpose

When an HTTP request fails, the first question is: "What exactly was sent?" `toCurl()` answers that question by rendering the prepared call as a copyable curl command.

## Basic Usage

```java
HttpCall<HttpResult> call = http.request(HttpMethod.POST, "https://api.example.com/users",
        Map.of("Content-Type", "application/json"),
        "{\"name\":\"Arthur\"}");

String curl = call.toCurl();
System.out.println(curl);
```

Output:

```
curl -X POST 'https://api.example.com/users' -H 'Content-Type: application/json' --data-raw '{"name":"Arthur"}'
```

## toCurl() Does Not Execute

Calling `toCurl()` never triggers a network request. It is safe to call before or after `execute()`:

```java
HttpCall<HttpResult> call = http.request(HttpMethod.GET, "https://api.example.com/users");

String curl = call.toCurl();  // no network call
HttpResult result = call.execute();  // network call happens here
String curlAgain = call.toCurl();  // same output, no network call
```

## Examples by Request Type

### GET

```java
http.request(HttpMethod.GET, "https://api.example.com/users").toCurl();
// curl 'https://api.example.com/users'
```

### POST with body and headers

```java
http.request(HttpMethod.POST, "https://api.example.com/users",
        Map.of("Content-Type", "application/json", "Authorization", "Bearer token"),
        "{\"name\":\"Arthur\"}").toCurl();
// curl -X POST 'https://api.example.com/users' -H 'Content-Type: application/json' -H 'Authorization: Bearer token' --data-raw '{"name":"Arthur"}'
```

### File upload

```java
http.upload(HttpMethod.POST, "https://api.example.com/files",
        Map.of("Content-Type", "application/octet-stream"),
        Path.of("document.pdf")).toCurl();
// curl -X POST 'https://api.example.com/files' -H 'Content-Type: application/octet-stream' --data-binary '@document.pdf'
```

### Download

```java
http.download(HttpMethod.GET, "https://api.example.com/files/123",
        Map.of(),
        Path.of("downloaded.pdf")).toCurl();
// curl 'https://api.example.com/files/123' -o 'downloaded.pdf'
```

### Multipart

```java
http.multipart(HttpMethod.POST, "https://api.example.com/upload",
        Map.of("Authorization", "Bearer token"),
        Map.of("name", "Arthur"),
        Map.of("avatar", Path.of("photo.jpg"))).toCurl();
// curl -X POST 'https://api.example.com/upload' -H 'Authorization: Bearer token' -F 'name=Arthur' -F 'avatar=@photo.jpg'
```

## Base64 Output with toCurlBase64()

`toCurlBase64()` returns the same curl command as `toCurl()`, but Base64-encoded using `java.util.Base64` and UTF-8.

```java
HttpCall<HttpResult> call = http.request(HttpMethod.POST, "https://api.example.com/users",
        Map.of("Content-Type", "application/json"),
        "{\"name\":\"Arthur\"}");

String encoded = call.toCurlBase64();
System.out.println(encoded);
```

### When to use it

- Logging curl commands to systems that truncate or distort raw text
- Copy/paste through chat, email, or ticket systems that mangle quotes or newlines
- JSON or XML bodies with many special characters
- Multiline curl commands that get reformatted

### Decode from shell

```bash
echo 'BASE64_VALUE' | base64 -d
```

### Decode from Java

```java
String curl = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
```

### Important

- **This is not encryption.** Base64 is an encoding. Anyone can decode it.
- **This does not mask secrets.** Authorization headers and bodies are encoded as-is.
- **Do not log sensitive requests in production.** Since v1 has no secret masking, the Base64 output still contains all headers and bodies.
- Like `toCurl()`, calling `toCurlBase64()` does not execute the request.

## Also Available in Exceptions

When a request fails, the curl is included in the exception:

```java
try {
    http.request(HttpMethod.POST, "https://api.example.com/users",
            Map.of("Content-Type", "application/json"),
            "{\"name\":\"Arthur\"}").execute();
} catch (HttpException e) {
    System.err.println("Curl: " + e.curl());
    // Copy this directly into your terminal to reproduce the request
}
```

## Limitations

1. **No secret masking in v1.** Authorization headers and bodies are rendered as-is. This is by design for maximum debugging usefulness.
2. **Binary body content** cannot be accurately represented. File uploads use `@path` syntax.
3. **File paths** use the path as provided. The file is not read during `toCurl()`.
4. **Unicode** is passed through as-is. Terminal encoding must support it.

## Shell Escaping

Values are wrapped in single quotes. Single quotes within values are escaped as `'\''`:

```
curl -X POST 'https://api.example.com/data' --data-raw 'it'\''s working'
```

This covers common cases. Highly unusual characters may require manual adjustment.

Use `HttpMethod` constants for standard methods. Raw method strings are accepted for custom or uncommon methods.
