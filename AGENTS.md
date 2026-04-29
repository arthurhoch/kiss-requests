# AGENTS.md

This file is the primary instruction document for AI agents working on the KissRequests repository.

Every AI agent must read this file before making any changes to the codebase.

## Fast Context File

Agents should read `CAVEMAN.md` first for a compact project summary, then read the detailed authoritative files:

- `AGENTS.md`
- `docs/PRODUCT_SPEC.md`
- `.github/architecture/index.md`
- `docs/IMPLEMENTATION_PLAN.md`

CAVEMAN.md is a summary only. If it conflicts with detailed docs, the detailed docs win.

## Project Purpose

KissRequests is a tiny, KISS-oriented Java 17+ HTTP library built on the native JDK `java.net.http.HttpClient`.

The library makes HTTP requests easy to write, memorize, debug, and execute — without requiring users to read long documentation or depend on external HTTP libraries.

The library is NOT a framework.

## Non-Negotiable KISS Rules

1. The public API must be memorable and obvious.
2. Zero mandatory external dependencies in the core library.
3. Java 17+ compatibility must be preserved at all times.
4. Use native `java.net.http.HttpClient` exclusively for HTTP transport.
5. Do not hide HTTP behind a large DSL.
6. Do not create annotations.
7. Do not create a REST framework.
8. Do not add JSON serialization to the core.
9. Do not add XML serialization to the core.
10. Do not add secret masking, credential management, OAuth helpers, token refresh, OpenTelemetry, circuit breaker, service discovery, cache, or framework integrations in v1.
11. The user passes method, URL, headers, and body.
12. The library prepares the request, renders it as curl, executes it, and returns a result or throws a rich exception.

## v1 Scope

- Text requests
- Binary file upload
- Download response directly to file
- Stream response as `InputStream`
- Multipart/form-data
- `toCurl()` for debugging
- `execute()` for execution
- Rich errors
- Retry configured in the singleton
- Timeout configured in the singleton
- Max concurrent requests configured in the singleton
- Optional `Executor` configured in the singleton
- Maven project
- Unit tests
- Documentation
- GitHub Actions CI
- GitHub Pages documentation
- Maven Central publishing configuration

## v1 Non-Goals

- JSON or XML serialization
- Annotation-driven API
- REST framework features
- Secret masking or credential management
- OAuth or token refresh
- Observability integrations (OpenTelemetry, Micrometer)
- Circuit breaker or resilience patterns
- Service discovery
- Cache layer
- Spring, Quarkus, or any framework integration
- Virtual threads as a required feature (may be used via user-provided Executor)

## Coding Rules

1. Java 17 source and target compatibility.
2. No production dependencies. Zero.
3. Use `java.net.http.HttpClient` for all HTTP transport.
4. Prefer simple records and classes over complex hierarchies.
5. Prefer explicit names over clever abstractions.
6. All public API classes must be in `io.github.arthurhoch.kissrequests` or a sub-package.
7. Internal implementation must be in `io.github.arthurhoch.kissrequests.internal` or equivalent, with clear separation.
8. No Lombok, no annotation processing, no code generation.
9. No reflection-based magic.
10. Thread safety must be documented for all public singletons.
11. `Http.create()` must return a usable default instance with no required configuration.
12. `http.request(...)` must return a prepared `HttpCall`, not execute immediately.
13. `.execute()` on `HttpCall` triggers the actual network call.
14. `.toCurl()` on `HttpCall` returns the curl representation without executing.
15. Errors must be rich: include method, URL, curl, attempts, duration, status code, response body, and root cause.
16. Interrupted threads must restore the interrupt flag.

## Testing Rules

1. All tests must use JUnit 5.
2. Tests must not require internet access.
3. Tests must not require external services.
4. Tests must be deterministic.
5. Use a local HTTP test server built with JDK APIs.
6. Every public method must have at least one test.
7. Tests must cover: text requests, upload, download, stream, multipart, toCurl, retry, timeout, rich exceptions, concurrency limit, invalid inputs.
8. Test failures must produce clear messages.

## Documentation Rules

1. All public API changes must be reflected in documentation.
2. All public API changes must be reflected in examples.
3. Documentation must be updated when public behavior changes.
4. Every public API must have at least one copyable example.
5. README.md is the quick start.
6. docs/ contains detailed documentation.
7. GitHub Pages serves the documentation site.
8. Documentation must prioritize "can use without reading a manual."
9. Code examples in documentation must use English names.
10. All documentation must be in English.

## GitHub Pages Rules

1. GitHub Pages is served from the `docs/` directory on the `main` branch.
2. Jekyll with a minimal theme is used.
3. No heavy frontend frameworks.
4. Markdown-first documentation.
5. docs/index.md is the entry point.

## Maven Central Publishing Rules

1. Maven coordinates: `io.github.arthurhoch:kiss-requests`.
2. Publishing uses Sonatype Central Publisher Portal.
3. Source JAR and Javadoc JAR are required.
4. GPG signing is required.
5. All required metadata (name, description, url, licenses, developers, scm) must be present in pom.xml.
6. Publishing only happens under the `release` Maven profile.
7. Publishing only happens via the release GitHub Actions workflow triggered by version tags.
8. CI must never require signing or publishing secrets.

## Release Rules

1. Semantic versioning: MAJOR.MINOR.PATCH.
2. Releases are triggered by tags matching `v*` (e.g., `v0.1.0`).
3. CHANGELOG.md must be updated before release.
4. The release workflow must run tests before publishing.
5. Do not release without updating documentation.

## AI Behavior Rules

1. Read `AGENTS.md`, `docs/PRODUCT_SPEC.md`, and `.github/architecture/index.md` before implementing code.
2. Do not invent features outside v1 scope.
3. Do not add dependencies unless explicitly approved by a human.
4. Do not create a framework.
5. Do not over-engineer.
6. Do not remove public API without documenting why.
7. Do not silently change behavior.
8. Always update docs and tests alongside code changes.
9. Prefer simple records/classes over complex hierarchies.
10. Prefer explicit names.
11. Preserve Java 17 compatibility.
12. When in doubt, choose the simpler solution.

## How to Execute Tasks Safely

1. Read this file first.
2. Read the relevant architecture documents for the area you are changing.
3. Read the product specification.
4. Make changes incrementally.
5. Run tests after every change.
6. Update documentation for every public behavior change.
7. Do not commit unless explicitly asked.
8. Do not push unless explicitly asked.
9. Report what you changed and what remains.

## Security Rules

1. Security tooling must not add production dependencies.
2. Normal CI (`mvn -B verify`) must stay fast. Security scans run in separate profiles or workflows.
3. Security scans and findings must be documented in `docs/SECURITY_SCANNING.md`.
4. Any vulnerability fix must include tests when applicable.
5. Do not suppress scanner findings without documenting the reason.
6. Do not commit secrets, tokens, or credentials.
7. If public behavior changes for security reasons, update docs and examples.
8. OWASP Dependency-Check runs via `mvn -Psecurity verify` only, not during normal builds.

## Required Reading for Implementation

Before implementing any code, an AI agent must read:

1. `AGENTS.md` (this file)
2. `docs/PRODUCT_SPEC.md`
3. `.github/architecture/index.md`

These three files define the contract. Implementation must follow them.

## Implementation Must Not

1. Add external dependencies without explicit justification and human approval.
2. Add framework patterns or integrations.
3. Break Java 17 compatibility.
4. Change the `http.request(...).execute()` mental model.
5. Remove `.toCurl()` from prepared calls.
6. Make errors vague or unhelpful.
7. Skip tests for public behavior.
8. Skip documentation updates.
