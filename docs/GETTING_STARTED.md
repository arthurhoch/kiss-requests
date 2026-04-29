# Getting Started

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.arthurhoch</groupId>
    <artifactId>kiss-requests</artifactId>
    <version>0.1.0</version>
</dependency>
```

## First GET

```java
import io.github.arthurhoch.kissrequests.Http;
import io.github.arthurhoch.kissrequests.HttpMethod;
import io.github.arthurhoch.kissrequests.HttpResult;

Http http = Http.create();
HttpResult result = http.request(HttpMethod.GET, "https://api.example.com/users").execute();
System.out.println(result.statusCode());
System.out.println(result.body());
```

## First POST

```java
Http http = Http.create();
HttpResult result = http.request(
        HttpMethod.POST,
        "https://api.example.com/users",
        Map.of("Content-Type", "application/json"),
        "{\"name\":\"Arthur\"}"
).execute();
System.out.println(result.statusCode());
System.out.println(result.body());
```

## Debug with curl

```java
HttpCall<HttpResult> call = http.request(
        HttpMethod.POST,
        "https://api.example.com/users",
        Map.of("Content-Type", "application/json"),
        "{\"name\":\"Arthur\"}"
);

String curl = call.toCurl();
System.out.println(curl);
// Output: curl -X POST 'https://api.example.com/users' -H 'Content-Type: application/json' --data-raw '{"name":"Arthur"}'

HttpResult result = call.execute();
```

## Handling errors

```java
try {
    HttpResult result = http.request(HttpMethod.GET, "https://api.example.com/missing").execute();
} catch (HttpException e) {
    System.err.println("Request failed: " + e.getMessage());
    System.err.println("Status: " + e.statusCode());
    System.err.println("Curl: " + e.curl());
    System.err.println("Attempts: " + e.attempts().size());
    System.err.println("Duration: " + e.totalDuration().toMillis() + "ms");
    System.err.println("Response: " + e.responseBody());
    System.err.println(e.report());
}
```

## Configuring the singleton

```java
Http http = Http.builder()
        .connectTimeout(Duration.ofSeconds(5))
        .requestTimeout(Duration.ofSeconds(60))
        .retryPolicy(RetryPolicy.of(3, Duration.ofMillis(500)))
        .maxConcurrentRequests(10)
        .executor(Executors.newCachedThreadPool())
        .build();
```

## Uploading a file

```java
HttpResult result = http.upload(HttpMethod.POST, "https://api.example.com/files",
        Map.of("Content-Type", "application/octet-stream"),
        Path.of("document.pdf")).execute();
System.out.println(result.statusCode());
```

## Downloading a file

```java
HttpDownloadResult result = http.download(HttpMethod.GET, "https://api.example.com/files/123",
        Map.of(),
        Path.of("downloaded.pdf")).execute();
System.out.println("Downloaded " + result.bytesWritten() + " bytes");
```

## Streaming a response

```java
HttpStreamResult result = http.stream(HttpMethod.GET, "https://api.example.com/stream",
        Map.of(), null).execute();
try (InputStream is = result.inputStream()) {
    is.transferTo(System.out);
}
```

## Multipart form

```java
HttpResult result = http.multipart(HttpMethod.POST, "https://api.example.com/upload",
        Map.of("Authorization", "Bearer token"),
        Map.of("name", "Arthur", "email", "arthur@example.com"),
        Map.of("avatar", Path.of("photo.jpg"))).execute();
System.out.println(result.statusCode());
```

Use `HttpMethod` constants for standard HTTP methods. Raw method strings are accepted when an API requires a custom or uncommon method.

## Next Steps

- [API Reference](API.md)
- [Examples](EXAMPLES.md)
- [Error Handling](ERROR_HANDLING.md)
- [Curl Debugging](CURL_DEBUGGING.md)
- [File Operations](FILE_UPLOAD_DOWNLOAD.md)
- [Configuration](CONFIGURATION.md)
