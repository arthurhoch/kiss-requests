# Examples

## GET

```java
Http http = Http.create();
HttpResult result = http.request(HttpMethod.GET, "https://api.example.com/users").execute();
System.out.println(result.statusCode()); // 200
System.out.println(result.body());       // [{"id":1,"name":"Arthur"}, ...]
```

## POST

```java
Http http = Http.create();
HttpResult result = http.request(
        HttpMethod.POST,
        "https://api.example.com/users",
        Map.of("Content-Type", "application/json"),
        "{\"name\":\"Arthur\"}"
).execute();
System.out.println(result.statusCode()); // 201
System.out.println(result.body());       // {"id":2,"name":"Arthur"}
```

## PUT

```java
Http http = Http.create();
HttpResult result = http.request(
        HttpMethod.PUT,
        "https://api.example.com/users/1",
        Map.of("Content-Type", "application/json"),
        "{\"name\":\"Arthur Updated\"}"
).execute();
System.out.println(result.statusCode()); // 200
```

## DELETE

```java
Http http = Http.create();
HttpResult result = http.request(
        HttpMethod.DELETE,
        "https://api.example.com/users/1"
).execute();
System.out.println(result.statusCode()); // 204
```

## Debug with toCurl()

```java
HttpCall<HttpResult> call = http.request(HttpMethod.POST, "https://api.example.com/users",
        Map.of("Content-Type", "application/json"),
        "{\"name\":\"Arthur\"}");

String curl = call.toCurl();
// curl -X POST 'https://api.example.com/users' -H 'Content-Type: application/json' --data-raw '{"name":"Arthur"}'

HttpResult result = call.execute();
```

## try/catch Error Handling

```java
try {
    HttpResult result = http.request(HttpMethod.GET, "https://api.example.com/users/999").execute();
} catch (HttpException e) {
    System.err.println("Failed: " + e.getMessage());
    System.err.println("Status: " + e.statusCode());          // 404
    System.err.println("Response: " + e.responseBody());      // {"error":"Not found"}
    System.err.println("Curl: " + e.curl());                  // curl 'https://api.example.com/users/999'
    System.err.println("Attempts: " + e.attempts().size());   // 1
    System.err.println("Duration: " + e.totalDuration().toMillis() + "ms");
    System.err.println(e.report());
}
```

## Inspecting Retry Attempts

```java
Http http = Http.builder()
        .retryPolicy(RetryPolicy.of(3, Duration.ofMillis(500)))
        .build();

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

## Upload a File

```java
Http http = Http.create();
HttpResult result = http.upload(HttpMethod.POST, "https://api.example.com/files",
        Map.of("Content-Type", "application/pdf"),
        Path.of("/path/to/document.pdf")).execute();
System.out.println(result.statusCode()); // 200
```

## Download a File

```java
Http http = Http.create();
HttpDownloadResult result = http.download(HttpMethod.GET, "https://api.example.com/files/123",
        Map.of(),
        Path.of("/path/to/output.pdf")).execute();
System.out.println("Downloaded " + result.bytesWritten() + " bytes to " + result.file());
```

Parent directories are created automatically if they do not exist.

## Stream a Response

```java
Http http = Http.create();
HttpStreamResult result = http.stream(HttpMethod.GET, "https://api.example.com/events",
        Map.of(), null).execute();
try (InputStream is = result.inputStream()) {
    is.transferTo(System.out);
}
```

## Multipart Form

```java
Http http = Http.create();
HttpResult result = http.multipart(HttpMethod.POST, "https://api.example.com/upload",
        Map.of("Authorization", "Bearer token"),
        Map.of("name", "Arthur", "email", "arthur@example.com"),
        Map.of("avatar", Path.of("/path/to/photo.jpg"))).execute();
System.out.println(result.statusCode()); // 200
```

## Configured Singleton

```java
Http http = Http.builder()
        .connectTimeout(Duration.ofSeconds(5))
        .requestTimeout(Duration.ofSeconds(60))
        .retryPolicy(RetryPolicy.of(3, Duration.ofMillis(500)))
        .maxConcurrentRequests(10)
        .executor(Executors.newCachedThreadPool())
        .build();

// Use the configured instance everywhere
HttpResult result = http.request(HttpMethod.GET, "https://api.example.com/users").execute();
```

## Debug with toCurlBase64()

Returns a Base64-encoded curl command. Useful for logs, multiline bodies, or environments that distort raw text.

```java
HttpCall<HttpResult> call = http.request(HttpMethod.POST, "https://api.example.com/users",
        Map.of("Content-Type", "application/json"),
        "{\"name\":\"Arthur\"}");

String encoded = call.toCurlBase64();
System.out.println(encoded);

// Decode in shell:
// echo 'ENCODED_VALUE' | base64 -d

// Decode in Java:
// String curl = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
```

Not encryption. Does not mask secrets. Do not log sensitive requests in production.

## toCurl for All Request Types

```java
// GET
http.request(HttpMethod.GET, "https://api.example.com/users").toCurl();
// curl 'https://api.example.com/users'

// POST
http.request(HttpMethod.POST, "https://api.example.com/users",
        Map.of("Content-Type", "application/json"),
        "{\"name\":\"Arthur\"}").toCurl();
// curl -X POST 'https://api.example.com/users' -H 'Content-Type: application/json' --data-raw '{"name":"Arthur"}'

// Upload
http.upload(HttpMethod.POST, "https://api.example.com/files",
        Map.of("Content-Type", "application/pdf"),
        Path.of("doc.pdf")).toCurl();
// curl -X POST 'https://api.example.com/files' -H 'Content-Type: application/pdf' --data-binary '@doc.pdf'

// Download
http.download(HttpMethod.GET, "https://api.example.com/files/123",
        Map.of(), Path.of("out.pdf")).toCurl();
// curl 'https://api.example.com/files/123' -o 'out.pdf'

// Multipart
http.multipart(HttpMethod.POST, "https://api.example.com/upload",
        Map.of(), Map.of("name", "Arthur"), Map.of("file", Path.of("photo.jpg"))).toCurl();
// curl -X POST 'https://api.example.com/upload' -F 'name=Arthur' -F 'file=@photo.jpg'
```

Use `HttpMethod` constants for standard HTTP methods. Raw strings are accepted when an API needs a custom or uncommon method, for example:

```java
HttpResult result = http.request("PROPFIND", url, Map.of(), null).execute();
```
