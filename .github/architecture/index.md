# Architecture Index

This directory contains the architecture documentation for KissRequests.

## Reading Order

AI agents and contributors should read these files in order:

1. **[00-product-purpose.md](00-product-purpose.md)** — Why this library exists, what pain it solves, who it is for.
2. **[01-public-api-contract.md](01-public-api-contract.md)** — The implemented public API contract for v1. Start here for API review.
3. **[02-kiss-rules.md](02-kiss-rules.md)** — KISS design rules that constrain all decisions.
4. **[03-core-architecture.md](03-core-architecture.md)** — Internal design, components, and responsibilities.
5. **[04-error-handling.md](04-error-handling.md)** — Rich error handling specification.
6. **[05-curl-rendering.md](05-curl-rendering.md)** — `.toCurl()` behavior specification.
7. **[06-file-and-stream-support.md](06-file-and-stream-support.md)** — File upload, download, stream, multipart design.
8. **[07-configuration-model.md](07-configuration-model.md)** — Singleton and configuration model.
9. **[08-testing-strategy.md](08-testing-strategy.md)** — Testing strategy and coverage plan.
10. **[09-documentation-policy.md](09-documentation-policy.md)** — Documentation standards.
11. **[10-release-and-maven-central.md](10-release-and-maven-central.md)** — Release and Maven Central publishing plan.
12. **[11-github-pages.md](11-github-pages.md)** — GitHub Pages documentation plan.

## Cross-References

- The product specification is in `docs/PRODUCT_SPEC.md`.
- The implementation plan is in `docs/IMPLEMENTATION_PLAN.md`.
- The review checklist is in `docs/REVIEW_CHECKLIST.md`.
- The root `AGENTS.md` contains project-wide rules that apply to all architecture decisions.

## Maintenance

When adding a new architecture document:

1. Add it to this index with the correct number and description.
2. Update `.github/ALL_MARKDOWN.md`.
3. Follow the naming convention: `NN-topic.md` where `NN` is a two-digit sequence number.
