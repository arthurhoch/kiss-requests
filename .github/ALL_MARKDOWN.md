# All Markdown Files Index

This file is a manual index of all markdown files in the repository. It must be updated whenever markdown files are added, removed, or renamed.

**Last updated: 2026-04-29**

## Root

| File | Purpose |
|---|---|
| `README.md` | Project overview, quick examples, scope, links. Entry point for GitHub visitors. |
| `CAVEMAN.md` | Compact low-token project summary for AI agents and quick human onboarding. |
| `AGENTS.md` | Primary instruction document for AI agents. Contains project rules, scope, coding/testing/docs/release rules. |
| `CHANGELOG.md` | Tracks all notable changes. Follows Keep a Changelog format. |
| `SECURITY.md` | Security policy. How to report vulnerabilities, supported versions, dependency policy. |
| `LICENSE` | Apache License 2.0 full text (not markdown, but included for completeness). |

## `.github/`

| File | Purpose |
|---|---|
| `.github/AGENTS.md` | GitHub-specific workflow rules supplementing root AGENTS.md. PR, branch, CI, release, docs rules. |
| `.github/copilot-instructions.md` | Instructions optimized for GitHub Copilot and coding agents. |
| `.github/ai-rules.md` | Strict AI behavior rules. Non-negotiable constraints for AI agents. |
| `.github/ALL_MARKDOWN.md` | This file. Manual index of all markdown files. |

## `.github/architecture/`

| File | Purpose |
|---|---|
| `.github/architecture/index.md` | Architecture documentation index with reading order. |
| `.github/architecture/00-product-purpose.md` | Library purpose, motivation, target users. |
| `.github/architecture/01-public-api-contract.md` | Implemented public API contract for v1. |
| `.github/architecture/02-kiss-rules.md` | KISS design rules and constraints. |
| `.github/architecture/03-core-architecture.md` | Internal design and component responsibilities. |
| `.github/architecture/04-error-handling.md` | Rich error handling specification. |
| `.github/architecture/05-curl-rendering.md` | `.toCurl()` behavior specification. |
| `.github/architecture/06-file-and-stream-support.md` | File upload, download, stream, multipart design. |
| `.github/architecture/07-configuration-model.md` | Singleton and configuration model. |
| `.github/architecture/08-testing-strategy.md` | Testing strategy and coverage plan. |
| `.github/architecture/09-documentation-policy.md` | Documentation standards and update rules. |
| `.github/architecture/10-release-and-maven-central.md` | Release and Maven Central publishing plan. |
| `.github/architecture/11-github-pages.md` | GitHub Pages documentation plan. |

## `docs/`

| File | Purpose |
|---|---|
| `docs/index.md` | GitHub Pages entry point. Links to all documentation. |
| `docs/PRODUCT_SPEC.md` | Complete product specification. The most important spec file. |
| `docs/GETTING_STARTED.md` | Getting started guide for users. |
| `docs/KISSREQUESTS_AI_USAGE.md` | Standalone guide for AI agents using KissRequests in consumer projects. |
| `docs/API.md` | Public API reference. |
| `docs/EXAMPLES.md` | Copyable code examples for all v1 features. |
| `docs/ERROR_HANDLING.md` | Error handling guide and examples. |
| `docs/CURL_DEBUGGING.md` | Curl debugging guide. |
| `docs/FILE_UPLOAD_DOWNLOAD.md` | File upload, download, stream, multipart guide. |
| `docs/CONFIGURATION.md` | Configuration guide. |
| `docs/RELEASE.md` | Release process documentation. |
| `docs/MAVEN_CENTRAL.md` | Maven Central publishing guide and required secrets. |
| `docs/IMPLEMENTATION_PLAN.md` | Step-by-step implementation plan for AI agents. |
| `docs/REVIEW_CHECKLIST.md` | Implementation, hardening, and release review checklist. |
| `docs/SECURITY_SCANNING.md` | Security scanning documentation. CodeQL, Dependabot, OWASP Dependency-Check. |
| `docs/SECRET_HYGIENE.md` | Rules for handling secrets, credentials, and curl output. |

## `.github/workflows/`

No markdown files. YAML workflow files:

- `.github/workflows/ci.yml`
- `.github/workflows/codeql.yml`
- `.github/workflows/pages.yml`
- `.github/workflows/release-maven-central.yml`
