# KissRequests

Tiny zero-dependency Java 17+ HTTP client library built on native `java.net.http.HttpClient`.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.arthurhoch/kiss-requests.svg)](https://central.sonatype.com/artifact/io.github.arthurhoch/kiss-requests)
[![Java](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://openjdk.org/projects/jdk/17/)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)
[![CI](https://github.com/arthurhoch/kiss-requests/actions/workflows/ci.yml/badge.svg)](https://github.com/arthurhoch/kiss-requests/actions/workflows/ci.yml)
[![CodeQL](https://github.com/arthurhoch/kiss-requests/actions/workflows/codeql.yml/badge.svg)](https://github.com/arthurhoch/kiss-requests/actions/workflows/codeql.yml)
[![Docs](https://github.com/arthurhoch/kiss-requests/actions/workflows/pages.yml/badge.svg)](https://github.com/arthurhoch/kiss-requests/actions/workflows/pages.yml)

Part of the KISS Java Libraries family: small, explicit, zero-dependency Java 17+ libraries. Each project is independent. Use only the modules you need.

## Status

Latest stable release: `0.1.0`.

Current development version: `0.1.1-SNAPSHOT`.

The `0.1.0` artifact is published on Maven Central and the `v0.1.0` GitHub release is available.

## Why this exists

KissRequests exists for Java projects that need a small HTTP client abstraction without bringing in Apache HttpClient, OkHttp, a REST framework, or a large DSL. It keeps the mental model simple: prepare a call, inspect it as curl when needed, execute it, and handle a rich result or exception.

## Installation

```xml
<dependency>
    <groupId>io.github.arthurhoch</groupId>
    <artifactId>kiss-requests</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Quick Start

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

## Design Principles

- KISS: keep HTTP calls explicit and easy to read.
- Zero production dependencies.
- Java 17+ standard APIs through native `HttpClient`.
- Small public API and no framework lock-in.
- Predictable execution: `.execute()` performs network I/O, `.toCurl()` does not.
- Rich errors with method, URL, curl, attempts, duration, status, headers, body, and cause.

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

## Related KISS Projects

These libraries are independent, zero-dependency Java 17+ projects. Use only the modules you need.

| Project | Purpose |
|---|---|
| [kiss-json](https://github.com/arthurhoch/kiss-json) | Field-based JSON serialization and deserialization. |
| [kiss-requests](https://github.com/arthurhoch/kiss-requests) | Simple HTTP client built on Java HttpClient. |
| [kiss-server](https://github.com/arthurhoch/kiss-server) | Small HTTP/1.1 server for simple REST-style applications. |
| [kiss-config](https://github.com/arthurhoch/kiss-config) | Configuration loading from properties, .env files, system properties, and environment variables. |
| [kiss-binary](https://github.com/arthurhoch/kiss-binary) | Explicit binary IO for primitive binary formats. |

## Documentation

- [GitHub Pages](https://arthurhoch.github.io/kiss-requests/)
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
- [Testing Report](docs/TESTING_REPORT.md)
- [Safe Code Cleanup](docs/code-cleanup.md)

## Architecture

- [Architecture Index](.github/architecture/index.md)

## Security

- [Security Policy](SECURITY.md)
- [Security Scanning](docs/SECURITY_SCANNING.md)
- [Secret Hygiene](docs/SECRET_HYGIENE.md)
- [Maven Central Publishing](docs/MAVEN_CENTRAL.md)

```bash
# Normal build (fast, no security scans)
mvn -B clean verify

# Run OWASP Dependency-Check (downloads vulnerability database)
mvn -Psecurity verify

# Generate coverage report
mvn -B test jacoco:report
```

CodeQL and Dependabot run automatically in GitHub Actions. No secrets required.

## Requirements

- Java 17 or newer.
- Maven for building from source.

## Build

```bash
mvn -B clean verify
mvn -B test jacoco:report
mvn -B javadoc:javadoc
```

Additional configured profiles:

```bash
mvn -Pspotbugs verify
mvn -Psecurity verify
```

## Quality, Coverage, and Release Checks

JaCoCo coverage is generated during `verify`. Read the HTML report at `target/site/jacoco/index.html`; use `target/site/jacoco/jacoco.xml` for Codecov or Sonar if those services are configured later. No coverage badge is shown until a real external coverage service is configured.

Before deleting code, follow [Safe Code Cleanup](docs/code-cleanup.md): distinguish internal code from public API, search source/tests/docs/examples, inspect coverage, run Javadocs, and document user-visible removals in `CHANGELOG.md`. Before release, run the normal build, Javadocs, coverage generation, and any relevant optional quality/security profiles.

## License

Apache License 2.0. See [LICENSE](LICENSE).

## Contributing

1. Read `AGENTS.md` in the repository root.
2. Read `docs/PRODUCT_SPEC.md`.
3. Read `.github/architecture/index.md`.
4. Follow the KISS rules documented in `.github/architecture/02-kiss-rules.md`.
5. Ensure all changes update documentation and tests.

Contributions from humans and AI agents are welcome, provided they respect the KISS philosophy and do not introduce framework patterns, hidden dependencies, or unnecessary complexity.
