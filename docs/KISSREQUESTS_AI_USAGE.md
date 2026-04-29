# KissRequests AI Usage Guide

## What This File Is

This file teaches AI agents how to use KissRequests inside another Java project.

Copy this file into the consuming project, paste it into an AI prompt, or provide its URL to the AI agent before asking it to write integration code.

This is not a development guide for the KissRequests library itself. Do not use this file as instructions for changing KissRequests internals.

## What KissRequests Is

KissRequests is a Java 17+ HTTP library with a small KISS API.

- It uses native `java.net.http.HttpClient` internally.
- It has no production dependencies.
- It prepares requests, renders curl, executes requests, and returns result objects.
- It is not a REST framework.
- It is not a JSON or XML mapper.
- It is not an OAuth or auth framework.
- It is not a logging framework.

## When To Use KissRequests

Use KissRequests for:

- External REST calls.
- Internal service calls.
- SOAP/XML over HTTP when the application manually provides the XML body.
- JSON over HTTP when the application serializes JSON itself.
- Binary file upload.
- File download.
- Streaming large response bodies.
- Multipart/form-data.
- Curl-debuggable requests.
- Projects that want no extra HTTP client dependency.

## When Not To Use KissRequests

Do not choose KissRequests when:

- The project needs a full reactive HTTP stack.
- The project requires framework-native clients.
- The project needs generated API clients.
- The project needs automatic object mapping built into the HTTP client.
- The project needs advanced auth or token refresh built into the client.
- The project already has a required standard HTTP client mandated by architecture.

## Maven Dependency

```xml
<dependency>
    <groupId>io.github.arthurhoch</groupId>
    <artifactId>kiss-requests</artifactId>
    <version>VERSION_HERE</version>
</dependency>
```

Replace `VERSION_HERE` with the released version.

If KissRequests is not released yet, install it from source or use the local Maven repository according to the consuming project's setup.

## Common Imports

```java
import io.github.arthurhoch.kissrequests.Http;
import io.github.arthurhoch.kissrequests.HttpCall;
import io.github.arthurhoch.kissrequests.HttpDownloadResult;
import io.github.arthurhoch.kissrequests.HttpException;
import io.github.arthurhoch.kissrequests.HttpMethod;
import io.github.arthurhoch.kissrequests.HttpResult;
import io.github.arthurhoch.kissrequests.HttpStreamResult;
import io.github.arthurhoch.kissrequests.RetryPolicy;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
```

## Main Mental Model

```java
HttpResult result = http.request(
        HttpMethod.POST,
        url,
        headers,
        body
).execute();
```

- `http.request(...)` prepares the call.
- `execute()` sends the request.
- `toCurl()` returns the curl command before execution.
- `toCurlBase64()` returns Base64 of `toCurl()` in versions that expose it.
- The AI should normally create one reusable `Http` instance per integration/config.

## Recommended Architecture In Consuming Projects

Use this layering:

```text
application service
    -> integration gateway/client
        -> KissRequests Http
```

Rules:

- Do not spread HTTP calls everywhere in business logic.
- Create small integration clients or gateways per external system.
- Keep KissRequests usage at the infrastructure/integration boundary.
- Keep domain and application logic independent from HTTP details.
- Serialize JSON and XML outside KissRequests.
- Parse responses outside KissRequests.
- Translate `HttpException` into application-specific exceptions when useful.

Example package layout:

```text
com.example.integration.payment.PaymentGateway
com.example.integration.weather.WeatherClient
com.example.integration.files.FileStorageClient
```

## Singleton/Config Rule

Create and reuse one `Http` instance per integration/config. Do not create a new `Http` instance for every request.

Use `Http.create()` for defaults. Use `Http.builder()` for timeouts, retry, concurrency, `Executor`, and other supported config. Keep configuration centralized.

```java
private static final Http HTTP = Http.builder()
        .connectTimeout(Duration.ofSeconds(3))
        .requestTimeout(Duration.ofSeconds(10))
        .maxConcurrentRequests(100)
        .build();
```

## Text Request

### GET

```java
HttpResult result = http.request(
        HttpMethod.GET,
        "https://api.example.com/users",
        Map.of("Accept", "application/json"),
        null
).execute();
```

For GET without a body, pass `null` body.

### POST JSON

```java
String json = "{\"name\":\"Arthur\"}";

HttpResult result = http.request(
        HttpMethod.POST,
        "https://api.example.com/users",
        Map.of(
                "Content-Type", "application/json",
                "Accept", "application/json"
        ),
        json
).execute();
```

For JSON, build the JSON string outside KissRequests. For XML or SOAP, build the XML string outside KissRequests.

## SOAP/XML Usage

KissRequests can send SOAP/XML as plain text.

It does not generate SOAP clients. It does not parse WSDL. It does not apply WS-Security. The application must build the envelope.

```java
String envelope = """
        <?xml version="1.0" encoding="UTF-8"?>
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
            <soapenv:Header/>
            <soapenv:Body>
                <example>value</example>
            </soapenv:Body>
        </soapenv:Envelope>
        """;

HttpResult result = http.request(
        HttpMethod.POST,
        "https://api.example.com/soap",
        Map.of(
                "Content-Type", "text/xml; charset=utf-8",
                "SOAPAction", "operation"
        ),
        envelope
).execute();
```

## Upload File

```java
HttpResult result = http.upload(
        HttpMethod.POST,
        "https://api.example.com/files",
        Map.of("Content-Type", "application/octet-stream"),
        Path.of("/tmp/file.bin")
).execute();
```

Use `upload(...)` for binary body upload. Do not manually read large files into a `String`. Set `Content-Type` explicitly when required by the API.

## Download File

```java
HttpDownloadResult result = http.download(
        HttpMethod.GET,
        "https://api.example.com/file.zip",
        Map.of(),
        Path.of("/tmp/file.zip")
).execute();
```

Use `download(...)` for large payloads saved to disk. Do not use a text request for large downloads.

## Stream Response

`HttpStreamResult` is not `AutoCloseable` in the current API. The caller must close `result.inputStream()`.

```java
HttpStreamResult result = http.stream(
        HttpMethod.GET,
        "https://api.example.com/events",
        Map.of("Accept", "application/json"),
        null
).execute();

try (InputStream input = result.inputStream()) {
    // Parse progressively here.
}
```

Use `stream(...)` when the response must be parsed progressively. The caller is responsible for closing stream resources.

## Multipart/Form-Data

```java
HttpResult result = http.multipart(
        HttpMethod.POST,
        "https://api.example.com/forms",
        Map.of(),
        Map.of("description", "Contract"),
        Map.of("file", Path.of("/tmp/contract.pdf"))
).execute();
```

Use `multipart(...)` for APIs expecting form fields and file fields. Do not manually build multipart unless the API requires custom behavior.

KissRequests sets the multipart `Content-Type` with boundary automatically. Do not set `Content-Type` manually for multipart requests.

## Curl Debugging

```java
HttpCall<HttpResult> call = http.request(
        HttpMethod.POST,
        url,
        headers,
        body
);

String curl = call.toCurl();

HttpResult result = call.execute();
```

- `toCurl()` does not execute the request.
- Use it before `execute()` for debugging.
- The curl output is intended to be copyable.
- KissRequests v1 does not mask secrets.
- Do not log sensitive curl output in production unless the application explicitly accepts the risk.

## Base64 Curl Debugging

`toCurlBase64()` is available in the current implemented API. For older versions, use this section only if `HttpCall` exposes `toCurlBase64()`.

`toCurlBase64()` returns Base64 of `toCurl()`. It is useful for logs where raw curl is distorted, filtered, multiline, or hard to copy.

Base64 is not encryption. It does not protect secrets.

```java
String curlBase64 = call.toCurlBase64();
```

Decode in terminal:

```bash
echo 'BASE64_VALUE' | base64 -d
```

Decode in Java:

```java
String decoded = new String(
        Base64.getDecoder().decode(call.toCurlBase64()),
        StandardCharsets.UTF_8
);
```

## Error Handling

```java
try {
    HttpResult result = http.request(
            HttpMethod.POST,
            url,
            headers,
            body
    ).execute();

} catch (HttpException e) {
    System.err.println(e.report());
    throw new ExternalServiceException("External request failed", e);
}
```

`execute()` may throw `HttpException`.

`HttpException` contains useful debugging data: method, URL, curl, attempts, duration, status when available, response headers when available, response body when available, and cause when available.

Consumer projects may translate `HttpException` into domain/application exceptions. Do not swallow `HttpException` silently.

## Retry

Configure retry on the `Http` singleton/client. Do not implement ad-hoc retry loops around every request unless required by application policy.

POST retry should be treated carefully. Do not retry non-idempotent operations unless the external API supports it or an idempotency key is used.

```java
Http http = Http.builder()
        .retryPolicy(RetryPolicy.of(3, Duration.ofMillis(500)))
        .build();
```

To explicitly configure retry status codes and methods:

```java
Http http = Http.builder()
        .retryPolicy(RetryPolicy.of(
                3,
                Duration.ofMillis(500),
                Set.of(429, 503),
                Set.of(HttpMethod.GET, HttpMethod.POST)
        ))
        .build();
```

Only include `POST` when the operation is safe to retry.

## Timeout

Configure connect timeout and request timeout centrally. Do not leave external integrations without timeout.

```java
Http http = Http.builder()
        .connectTimeout(Duration.ofSeconds(3))
        .requestTimeout(Duration.ofSeconds(10))
        .build();
```

## Concurrency

Use `maxConcurrentRequests(...)` when protecting the application or external API.

Do not create manual thread pools unless needed. If using an `Executor`, pass it through the builder.

```java
Executor executor = /* application-provided executor */;

Http http = Http.builder()
        .maxConcurrentRequests(100)
        .executor(executor)
        .build();
```

## What The AI Should Not Do

- Do not add Apache HttpClient just to make a request.
- Do not add OkHttp just to make a request.
- Do not add Spring WebClient or RestTemplate unless the project architecture requires Spring-native integration.
- Do not add Jackson/Gson to KissRequests usage examples unless the consuming application already uses it for JSON.
- Do not put JSON serialization inside KissRequests.
- Do not build another wrapper DSL over KissRequests unless there is a real integration boundary reason.
- Do not create one `Http` instance per request.
- Do not log raw curl or Base64 curl containing secrets in production unless explicitly approved.
- Do not retry POST blindly.
- Do not read large files into memory for upload/download.

## Minimal Prompt For AI Agents

Use KissRequests for HTTP calls in this Java project. Create one reusable Http instance per external integration/config. Use `http.request(method, url, headers, body).execute()` for text requests. Use `http.upload(...)` for binary uploads, `http.download(...)` for large downloads, `http.stream(...)` for progressive InputStream responses, and `http.multipart(...)` for multipart/form-data. Use `call.toCurl()` for debugging before `execute()`. Use `call.toCurlBase64()` only if available and only for debugging logs; Base64 is not encryption. Catch `HttpException` and translate it to application-specific exceptions when useful. Do not add Apache HttpClient, OkHttp, Spring WebClient, RestTemplate, Jersey, JSON mapping, OAuth helpers, telemetry, cache, circuit breaker, or another HTTP abstraction unless the project explicitly requires it. Keep JSON/XML serialization outside KissRequests. Keep HTTP logic inside integration gateway/client classes, not domain logic.

## Checklist For AI Changes In Consuming Projects

- Is there one reusable `Http` instance?
- Is the request built with method, URL, headers, and body?
- Is `execute()` inside appropriate try/catch?
- Is timeout configured?
- Is retry policy appropriate?
- Is POST retry avoided unless safe?
- Is `toCurl()` used only for debug?
- Are large files handled with `upload(...)`, `download(...)`, or `stream(...)`?
- Are no unnecessary HTTP dependencies added?
- Is integration code isolated from business/domain logic?
