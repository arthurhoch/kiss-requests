# Testing Report

Date: 2026-05-08

## What Was Tested

- Text requests built on `java.net.http.HttpClient`, including methods, headers, query parameters, request bodies, and prepared call execution.
- Curl rendering through `.toCurl()` and `.toCurlBase64()` without performing network I/O.
- Error reporting with method, URL, curl command, attempts, duration, status, headers, body, and cause context.
- File upload, file download, response streaming, and multipart/form-data behavior.
- Configuration, retries, timeouts, concurrency limits, and optional executor handling.
- Project sanity checks for Java 17 compilation, jar/source/javadoc artifacts, Javadocs, and zero production dependency expectations.

## Commands Run

```bash
mvn -B clean verify
mvn -B test jacoco:report
mvn -B javadoc:javadoc
mvn -B -Pspotbugs -DskipTests verify
mvn -B -Psecurity -Ddependency-check.skip=true -DskipTests verify
```

Results:

- `mvn -B clean verify`: passing, 86 tests, 0 failures, 0 errors.
- `mvn -B test jacoco:report`: passing; reports generated at `target/site/jacoco/jacoco.xml` and `target/site/jacoco/index.html`.
- `mvn -B javadoc:javadoc`: passing.
- `mvn -B -Pspotbugs -DskipTests verify`: passing profile validation.
- `mvn -B -Psecurity -Ddependency-check.skip=true -DskipTests verify`: passing profile validation with Dependency-Check database scanning intentionally skipped.

## Known Limits

- Tests use local HTTP server fixtures and do not validate third-party network behavior.
- The suite intentionally does not include JSON/XML mapping, OAuth helpers, or telemetry because those are outside the library scope.
- The JDK test HTTP server can emit a HEAD-response warning during tests; it did not fail the build.

## Future Tests Recommended

- Add more proxy and redirect edge cases if those features expand.
- Add larger file transfer fixtures where runtime remains reasonable.
- Add more timeout and retry combinations around interrupted or closed connections.
