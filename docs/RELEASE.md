# Release Process

## Semantic Versioning

KissRequests follows [Semantic Versioning](https://semver.org/):

- **MAJOR**: Breaking public API changes.
- **MINOR**: New features, backward-compatible.
- **PATCH**: Bug fixes, backward-compatible.

## Tag-Based Release

Releases are triggered by pushing a tag matching `v*`:

```bash
git tag v0.1.0
git push origin v0.1.0
```

## Pre-Release Checklist

Before creating a release tag:

1. [ ] All tests pass: `mvn -B verify`
2. [ ] CHANGELOG.md updated with the release version and date
3. [ ] Documentation updated for all public behavior changes
4. [ ] Version in pom.xml is the release version (`0.1.0` for the first release)
5. [ ] Commit the release-ready state
6. [ ] Tag the commit: `git tag v0.1.0`
7. [ ] Push the tag: `git push origin v0.1.0`

## Release Workflow

The GitHub Actions release workflow (`.github/workflows/release-maven-central.yml`) is triggered by the tag.

Steps:
1. Checkout the repository.
2. Set up Java 17.
3. Run `mvn -B verify` (tests must pass).
4. Import the GPG key.
5. Run `mvn -B deploy -P release` (builds, signs, publishes).

## Post-Release

After a successful release:

1. Verify the artifact on [Maven Central](https://central.sonatype.com/) (may take time).
2. Update the version in pom.xml to the next `-SNAPSHOT`:
   ```xml
   <version>0.2.0-SNAPSHOT</version>
   ```
3. Commit the snapshot version bump.

## Changelog

CHANGELOG.md follows [Keep a Changelog](https://keepachangelog.com/):

```markdown
## [0.1.0] - 2026-04-29

### Added
- Initial release.
- Text requests (GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS).
- File upload, download, stream, multipart.
- toCurl() debugging.
- Rich error handling.
- Retry, timeout, concurrency configuration.
```

## First Release

The first release (v0.1.0) requires:

1. Maven Central account and namespace verification.
2. GPG key setup.
3. GitHub secrets configuration.
4. See [Maven Central Publishing](MAVEN_CENTRAL.md) for details.

## Local Consumer Test Before Publishing

Before publishing to Maven Central, install the release artifact into the local Maven repository:

```bash
mvn -B clean install
```

Then create a separate temporary Maven project and depend on the local artifact:

```xml
<dependency>
    <groupId>io.github.arthurhoch</groupId>
    <artifactId>kiss-requests</artifactId>
    <version>0.1.0</version>
</dependency>
```

Minimal consumer example:

```java
import io.github.arthurhoch.kissrequests.Http;
import io.github.arthurhoch.kissrequests.HttpMethod;
import io.github.arthurhoch.kissrequests.HttpResult;

import java.util.Map;

class Example {
    public static void main(String[] args) {
        Http http = Http.create();
        HttpResult result = http.request(
                HttpMethod.GET,
                "https://api.example.com/status",
                Map.of("Accept", "text/plain"),
                null
        ).execute();

        System.out.println(result.statusCode());
        System.out.println(result.body());
    }
}
```

This tests the same coordinates that consumers will use after Central publication, but resolves them from the local Maven repository.
