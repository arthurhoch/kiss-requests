# GitHub Copilot Instructions

Use [CAVEMAN.md](../CAVEMAN.md) as the compact project summary. Do not treat it as a replacement for detailed rules.

## Library Purpose

KissRequests is a tiny, KISS-oriented Java 17+ HTTP library built on the native JDK `java.net.http.HttpClient`.

It makes HTTP requests easy to write, memorize, debug, and execute — with zero mandatory external dependencies.

## Public API Direction

The central mental model:

```java
Http http = Http.create();
HttpCall<HttpResult> call = http.request(HttpMethod.POST, "https://api.example.com/users",
        Map.of("Content-Type", "application/json"),
        "{\"name\":\"Arthur\"}");
String curl = call.toCurl();
HttpResult result = call.execute();
```

- `Http.create()` returns a usable default instance.
- `http.request(...)` returns a prepared `HttpCall`, does NOT execute.
- `call.execute()` triggers the actual network call.
- `call.toCurl()` returns a curl representation without executing.
- Errors throw `HttpException` with method, URL, curl, attempts, duration, status, response headers, body.

Additional v1 operations: upload, download, stream, multipart.

## Package Structure

- Base package: `io.github.arthurhoch.kissrequests`
- Public API: `io.github.arthurhoch.kissrequests`
- Internal: `io.github.arthurhoch.kissrequests.internal`
- Do not introduce extra public subpackages unless they simplify the API.

## KISS Constraints

- Zero mandatory external dependencies in the core library.
- Java 17+ compatibility.
- Native `java.net.http.HttpClient` only.
- No DSL explosion. No annotations. No reflection magic.
- No framework patterns. Not a REST framework.
- No JSON/XML serialization in the core.
- The user passes method, URL, headers, and body.
- The library prepares, renders as curl, executes, returns result or throws rich exception.

## Testing Expectations

- JUnit 5 for all tests.
- No internet access required. No external services.
- Local HTTP test server using JDK APIs.
- Tests must be deterministic.
- Every public method must have at least one test.
- Cover: text, upload, download, stream, multipart, toCurl, retry, timeout, errors, concurrency, invalid inputs.

## Documentation Expectations

- README.md for quick start.
- docs/ for detailed documentation.
- Every public API must have at least one copyable example.
- Update docs when public behavior changes.
- English only.

## Forbidden Additions

- No production dependencies (no Apache HttpClient, OkHttp, Jackson, Gson, SLF4J, etc.).
- No Lombok, no annotation processing, no code generation.
- No Spring, Quarkus, or framework integrations.
- No OAuth, circuit breaker, OpenTelemetry, Micrometer, cache, service discovery.
- No secret masking, credential management, token refresh.
- No virtual threads as a required feature.

## Preferred Java Style

- Java 17 source and target.
- Prefer records for immutable data carriers.
- Prefer simple classes over complex hierarchies.
- Prefer explicit names over clever abstractions.
- No reflection-based magic.
- Thread safety documented for singletons.
- Clean, readable, professional code.
