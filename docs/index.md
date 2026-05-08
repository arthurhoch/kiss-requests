# KissRequests Documentation

Tiny zero-dependency Java 17+ HTTP client library built on native `java.net.http.HttpClient`.

Part of the KISS Java Libraries family.

## Install

```xml
<dependency>
    <groupId>io.github.arthurhoch</groupId>
    <artifactId>kiss-requests</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Quick Example

```java
Http http = Http.create();
HttpResult result = http.request(HttpMethod.GET, "https://api.example.com/users").execute();
System.out.println(result.body());
```

## Core Features

- Text requests with standard or custom HTTP methods.
- Binary file upload, file download, response streaming, and multipart/form-data.
- Prepared calls with `.execute()`, `.toCurl()`, and `.toCurlBase64()`.
- Rich `HttpException` reports with method, URL, curl, attempts, duration, status, headers, body, and cause.
- Configurable retry, timeout, concurrency, and optional executor.

## Documentation

- [Getting Started](GETTING_STARTED.md) — Installation, first request, debugging.
- [KissRequests AI Usage Guide](KISSREQUESTS_AI_USAGE.md) — Standalone guide for AI agents using KissRequests in consumer projects.
- [API Reference](API.md) — Public API reference.
- [Examples](EXAMPLES.md) — Copyable code examples for every feature.
- [Error Handling](ERROR_HANDLING.md) — How errors work and how to handle them.
- [Curl Debugging](CURL_DEBUGGING.md) — Using `toCurl()` for debugging.
- [File Upload / Download / Stream / Multipart](FILE_UPLOAD_DOWNLOAD.md) — Working with files and streams.
- [Configuration](CONFIGURATION.md) — Configuring timeouts, retries, concurrency.

## Project

- [Product Specification](PRODUCT_SPEC.md) — Full product spec (for contributors and AI agents).
- [Implementation Plan](IMPLEMENTATION_PLAN.md) — Step-by-step implementation roadmap.
- [Review Checklist](REVIEW_CHECKLIST.md) — Implementation, hardening, and release review checklist.
- [Testing Report](TESTING_REPORT.md) — Current verification results and known limits.
- [Release Guide](RELEASE.md) — Release process documentation.
- [Maven Central Publishing](MAVEN_CENTRAL.md) — Publishing guide.

## Related KISS Projects

These libraries are independent, zero-dependency Java 17+ projects. Use only the modules you need.

| Project | Purpose |
|---|---|
| [kiss-json](https://github.com/arthurhoch/kiss-json) | Field-based JSON serialization and deserialization. |
| [kiss-requests](https://github.com/arthurhoch/kiss-requests) | Simple HTTP client built on Java HttpClient. |
| [kiss-server](https://github.com/arthurhoch/kiss-server) | Small HTTP/1.1 server for simple REST-style applications. |
| [kiss-config](https://github.com/arthurhoch/kiss-config) | Configuration loading from properties, .env files, system properties, and environment variables. |
| [kiss-binary](https://github.com/arthurhoch/kiss-binary) | Explicit binary IO for primitive binary formats. |

## Security

- [Security Policy](../SECURITY.md) — How to report vulnerabilities.
- [Security Scanning](SECURITY_SCANNING.md) — CodeQL, Dependabot, OWASP Dependency-Check.
- [Secret Hygiene](SECRET_HYGIENE.md) — Rules for handling secrets and credentials.

## Philosophy

- **KISS**: Keep It Simple, Stupid.
- **Zero dependencies**: No external libraries required.
- **Native JDK**: Built on `java.net.http.HttpClient`.
- **Memorable API**: Write requests from memory.
- **Debuggable**: Every call can be rendered as curl.
- **Rich errors**: Exceptions contain everything you need to debug.

## Links

- [GitHub](https://github.com/arthurhoch/kiss-requests)
- [Maven Central](https://central.sonatype.com/artifact/io.github.arthurhoch/kiss-requests)
- [Changelog](https://github.com/arthurhoch/kiss-requests/blob/main/CHANGELOG.md)
- [Security Policy](https://github.com/arthurhoch/kiss-requests/blob/main/SECURITY.md)
