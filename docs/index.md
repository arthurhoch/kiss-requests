# KissRequests Documentation

A tiny, KISS-oriented Java 17+ HTTP library built on the native JDK `HttpClient`.

## Quick Example

```java
Http http = Http.create();
HttpResult result = http.request(HttpMethod.GET, "https://api.example.com/users").execute();
System.out.println(result.body());
```

## Installation

```xml
<dependency>
    <groupId>io.github.arthurhoch</groupId>
    <artifactId>kiss-requests</artifactId>
    <version>0.1.0</version>
</dependency>
```

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
- [Release Guide](RELEASE.md) — Release process documentation.
- [Maven Central Publishing](MAVEN_CENTRAL.md) — Publishing guide.

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
