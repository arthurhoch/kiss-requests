---
layout: default
---

# Safe Code Cleanup

KissRequests is a small Java 17 HTTP client library with a published public API. Cleanup work must preserve the request execution model and avoid accidental API removal.

## Public API First

Treat these as public API unless a maintainer explicitly changes the contract:

- public types and methods under `io.github.arthurhoch.kissrequests`;
- documented request, upload, download, stream, multipart, curl, retry, timeout, and result APIs;
- examples in `README.md`, `docs/`, and GitHub Pages.

Do not remove public API directly after a Maven Central release without considering a deprecation cycle. Prefer deprecation first, update documentation, and remove only in a planned compatible release window.

## Reference Search

Before deleting anything, search references in all consumer-facing and maintainer-facing surfaces:

```bash
rg "SymbolName|methodName" src README.md docs .github
```

Include source, tests, README, docs, examples, architecture notes, GitHub Pages content, and release documentation. A code element with low coverage is not automatically unused.

## Required Checks

Run the normal verification and inspect coverage:

```bash
mvn -B clean verify
mvn -B test jacoco:report
mvn -B javadoc:javadoc
```

Coverage reports are generated at:

```text
target/site/jacoco/jacoco.xml
target/site/jacoco/index.html
```

Open `target/site/jacoco/index.html` for human review. Use the XML report for Codecov or Sonar if those services are configured later.

## Advanced Profiles

Use optional profiles as evidence, not as automatic deletion authority:

```bash
mvn -Pspotbugs verify
mvn -Psecurity verify
```

`spotbugs` generates SpotBugs reports without making normal CI depend on static-analysis findings. `security` runs OWASP Dependency-Check and may download vulnerability data.

No benchmark, PIT mutation-testing, or API-compatibility profile is configured yet. If public API removal is planned, add a japicmp or Revapi baseline against the previous Maven Central release first and keep the initial check non-failing until the baseline is reviewed.

## Cleanup Policy

- Distinguish internal implementation from public API before deletion.
- Never delete public API directly after release without deprecation review.
- Use OpenRewrite, IDE inspections, and static analyzers only as suggestions.
- Verify Javadocs after cleanup so public docs still build.
- Run API compatibility checks before public API removal once a baseline profile exists.
- Run PIT or focused mutation tests for critical retry, timeout, curl, and local-server behavior if deletion touches risky logic.
- Document the removal in `CHANGELOG.md` before release.

## Before Release

Before releasing a cleanup change, confirm:

- `mvn -B clean verify` passes.
- `mvn -B javadoc:javadoc` passes.
- JaCoCo XML and HTML reports are generated.
- Optional quality/security profiles were run or intentionally skipped with a documented reason.
- Public API removals went through deprecation or compatibility review.
- README, docs, examples, and GitHub Pages references are updated.
