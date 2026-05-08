# Contributing

## Before You Start

Read `AGENTS.md`, `CAVEMAN.md`, `docs/PRODUCT_SPEC.md`, and `.github/architecture/index.md` before making non-trivial changes.

KissRequests is a tiny, zero-production-dependency Java 17+ HTTP client library built on `java.net.http.HttpClient`. Contributions must keep the API memorable, explicit, and framework-free.

## Build

```bash
mvn -B verify
```

For release, public API, Javadoc, or security-sensitive changes, also run the relevant documented checks:

```bash
mvn -B javadoc:javadoc
mvn -Psecurity verify
```

## Rules

- Keep zero production dependencies.
- Preserve Java 17 compatibility.
- Use native `java.net.http.HttpClient` for HTTP transport.
- Do not add JSON/XML mapping, annotations, OAuth helpers, secret masking, telemetry, caches, circuit breakers, service discovery, or framework integrations to the core.
- Keep `.execute()` as the operation that performs network I/O and `.toCurl()` as a non-executing debug view.
- Update tests, docs, examples, Javadocs, and `CHANGELOG.md` for public behavior changes.
- Do not commit secrets, `target/`, IDE files, local logs, `.DS_Store`, or generated build output.

## Dependency Changes

Production dependencies are not allowed. Test, release, security, and build plugins must stay isolated from the published runtime artifact and must be documented when they affect contributor workflows.

## Documentation

Documentation must match the implemented public API. Examples should be copyable and should not imply secret masking or credential management exists in the library.
