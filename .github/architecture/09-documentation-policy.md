# 09 — Documentation Policy

This document defines the documentation standards for KissRequests.

## Principles

1. Documentation must prioritize "can use without reading a manual."
2. Every public API must have at least one copyable example.
3. Code examples must use English names.
4. All documentation must be in English.
5. Documentation must be updated when public behavior changes.

## Documentation Structure

### README.md (Quick Start)

- Project purpose in one sentence.
- Current implementation/release status.
- Quick examples: GET, POST, toCurl, error handling.
- v1 scope summary.
- Non-goals summary.
- Links to docs/.

### docs/index.md (GitHub Pages Entry Point)

- Introduction.
- Links to all documentation pages.
- Quick example.
- Installation instructions.

### docs/PRODUCT_SPEC.md (Product Specification)

- Complete product specification.
- Mission, problem statement, target users.
- v1 scope and non-goals.
- API philosophy.
- All API examples.
- Edge cases and acceptance criteria.
- Implementation roadmap.
- Review checklist.

### docs/GETTING_STARTED.md (Getting Started Guide)

- Installation (Maven coordinates).
- First GET.
- First POST.
- Debug with curl.
- Handle errors.
- Configure singleton.
- Upload file.
- Download file.
- Stream response.
- Multipart form.

### docs/API.md (API Reference)

- Public API reference for all v1 classes and methods.
- Reflects the implemented Java API.
- Updated when API changes.

### docs/EXAMPLES.md (Copyable Examples)

- Complete, copyable code examples for every v1 feature.
- GET, POST, PUT, DELETE.
- toCurl.
- Error handling.
- Upload, download, stream, multipart.
- Configured singleton.

### docs/ERROR_HANDLING.md (Error Handling Guide)

- How errors are thrown.
- HttpException structure.
- Error categories.
- Examples for each error category.
- Retry attempt inspection.

### docs/CURL_DEBUGGING.md (Curl Debugging Guide)

- How to use toCurl().
- Examples for each request type.
- Limitations.
- Shell escaping behavior.

### docs/FILE_UPLOAD_DOWNLOAD.md (File/Stream/Multipart Guide)

- Upload API and examples.
- Download API and examples.
- Stream API and examples.
- Multipart API and examples.
- Memory safety notes.

### docs/CONFIGURATION.md (Configuration Guide)

- Default configuration.
- Builder API.
- All config options.
- Retry policy configuration.
- Timeout configuration.
- Concurrency configuration.
- Executor configuration.

### docs/RELEASE.md (Release Guide)

- Release process.
- Semantic versioning.
- Tag-based release.
- Changelog requirements.

### docs/MAVEN_CENTRAL.md (Maven Central Publishing Guide)

- Publishing workflow.
- Required secrets.
- Manual setup steps.
- Verification steps.

### docs/IMPLEMENTATION_PLAN.md (Implementation Plan)

- Step-by-step plan for implementation.
- Phased approach with tests and docs at each phase.

### docs/REVIEW_CHECKLIST.md (Review Checklist)

- Checklist for implementation, hardening, and release reviews.

## Update Rules

1. When a public method is added: update API.md, EXAMPLES.md, GETTING_STARTED.md if relevant.
2. When a public method's behavior changes: update all docs that reference it.
3. When a public method is removed: update all docs, add CHANGELOG.md entry.
4. When a config option is added: update CONFIGURATION.md, API.md.
5. When error behavior changes: update ERROR_HANDLING.md.
6. Standard HTTP method examples should use `HttpMethod` constants. Raw method strings should appear only when documenting custom or uncommon methods.

## Markdown Standards

1. Use GitHub-flavored markdown.
2. Code blocks must specify the language (`java`, `xml`, `bash`).
3. Headers must follow a logical hierarchy (h1 for title, h2 for sections, h3 for subsections).
4. Tables for structured data.
5. No emoji in documentation unless explicitly requested.

## GitHub Pages

1. Source: `docs/` directory on `main` branch.
2. Theme: Jekyll with minimal theme.
3. Entry point: `docs/index.md`.
4. See `.github/architecture/11-github-pages.md` for details.
