# KissRequests

A tiny, KISS-oriented Java 17+ HTTP library built on the native JDK `java.net.http.HttpClient`.

Zero mandatory external dependencies. No framework. No magic.

## Status

**v0.1.0 — release ready.**  
All core features are implemented and covered by local tests. See [CHANGELOG.md](CHANGELOG.md) for details.

## Quick Examples

### GET

```java
Http http = Http.create();
HttpResult result = http.request(HttpMethod.GET, "https://api.example.com/users").execute();
System.out.println(result.body());
```

### POST

```java
Http http = Http.create();
HttpResult result = http.request(
        HttpMethod.POST,
        "https://api.example.com/users",
        Map.of("Content-Type", "application/json"),
        "{\"name\":\"Arthur\"}"
).execute();
System.out.println(result.statusCode());
```

### Debug with curl

```java
HttpCall<HttpResult> call = http.request(
        HttpMethod.POST,
        "https://api.example.com/users",
        Map.of("Content-Type", "application/json"),
        "{\"name\":\"Arthur\"}"
);

String curl = call.toCurl();
System.out.println(curl);

String curlBase64 = call.toCurlBase64();
System.out.println(curlBase64);

HttpResult result = call.execute();
```

### Handle errors

```java
try {
    HttpResult result = http.request(HttpMethod.GET, "https://api.example.com/missing").execute();
} catch (HttpException e) {
    System.err.println("Request failed: " + e.getMessage());
    System.err.println("Status: " + e.statusCode());
    System.err.println("Curl: " + e.curl());
    System.err.println("Attempts: " + e.attempts());
    System.err.println(e.report());
}
```

Use `HttpMethod.GET`, `HttpMethod.POST`, and the other `HttpMethod` constants for standard methods. Raw strings are still accepted for custom or uncommon methods.

## Philosophy

- **KISS**: Keep It Simple, Stupid.
- **Zero mandatory external dependencies** in the core library.
- **Native JDK `HttpClient`** under the hood.
- **No framework, no annotations, no DSL explosion.**
- **Easy enough to use without reading documentation.**
- The user passes method, URL, headers, and body.
- The library prepares the request, renders it as curl, executes it, and returns a result or throws a rich exception.

## v1 Scope

- Text requests (GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS)
- Binary file upload
- Download response directly to file
- Stream response as `InputStream`
- Multipart/form-data
- `toCurl()` for debugging
- `execute()` for execution
- Rich errors with method, URL, curl, attempts, duration, status, headers, body
- Retry with configurable policy
- Timeout configuration
- Max concurrent requests
- Optional `Executor`
- Maven Central publishing
- GitHub Actions CI
- GitHub Pages documentation

## Non-Goals

- No JSON serialization in the core
- No XML serialization in the core
- No annotation-driven API
- No REST framework
- No secret masking or credential management
- No OAuth helpers or token refresh
- No OpenTelemetry, Micrometer, or observability integrations
- No circuit breaker or service discovery
- No cache layer
- No Spring, Quarkus, or framework integrations
- No dependency on Apache HttpClient, OkHttp, or similar

## Documentation

- [CAVEMAN.md](CAVEMAN.md) — compact project summary for quick context
- [Documentation Index](docs/index.md)
- [KissRequests AI Usage Guide](docs/KISSREQUESTS_AI_USAGE.md) — standalone guide for AI agents using KissRequests in consumer projects
- [Product Specification](docs/PRODUCT_SPEC.md)
- [Getting Started](docs/GETTING_STARTED.md)
- [API Reference](docs/API.md)
- [Examples](docs/EXAMPLES.md)
- [Error Handling](docs/ERROR_HANDLING.md)
- [Curl Debugging](docs/CURL_DEBUGGING.md)
- [File Upload / Download / Stream / Multipart](docs/FILE_UPLOAD_DOWNLOAD.md)
- [Configuration](docs/CONFIGURATION.md)
- [Implementation Plan](docs/IMPLEMENTATION_PLAN.md)
- [Release Guide](docs/RELEASE.md)
- [Maven Central Publishing](docs/MAVEN_CENTRAL.md)
- [Review Checklist](docs/REVIEW_CHECKLIST.md)

## Architecture

- [Architecture Index](.github/architecture/index.md)

## Security

- [Security Policy](SECURITY.md)
- [Security Scanning](docs/SECURITY_SCANNING.md)
- [Secret Hygiene](docs/SECRET_HYGIENE.md)
- [Maven Central Publishing](docs/MAVEN_CENTRAL.md)

```bash
# Normal build (fast, no security scans)
mvn -B verify

# Run OWASP Dependency-Check (downloads vulnerability database)
mvn -Psecurity verify
```

CodeQL and Dependabot run automatically in GitHub Actions. No secrets required.

## Maven Coordinates

```xml
<dependency>
    <groupId>io.github.arthurhoch</groupId>
    <artifactId>kiss-requests</artifactId>
    <version>0.1.0</version>
</dependency>
```

## License

Apache License 2.0. See [LICENSE](LICENSE).

## Contributing

1. Read `AGENTS.md` in the repository root.
2. Read `docs/PRODUCT_SPEC.md`.
3. Read `.github/architecture/index.md`.
4. Follow the KISS rules documented in `.github/architecture/02-kiss-rules.md`.
5. Ensure all changes update documentation and tests.

Contributions from humans and AI agents are welcome, provided they respect the KISS philosophy and do not introduce framework patterns, hidden dependencies, or unnecessary complexity.
