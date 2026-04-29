# Review Checklist

Use this checklist before asking an AI agent to implement, harden, or release code.

## API Shape

- [ ] `Http.create()` returns a usable default instance.
- [ ] `http.request(...)` returns `HttpCall`, does not execute.
- [ ] `call.execute()` triggers the network call.
- [ ] `call.toCurl()` returns curl without executing.
- [ ] `http.upload(...)`, `http.download(...)`, `http.stream(...)`, `http.multipart(...)` follow the same pattern.
- [ ] Result types are correct: `HttpResult`, `HttpDownloadResult`, `HttpStreamResult`.
- [ ] `HttpException` contains method, URL, curl, attempts, duration, status, response headers, body, root cause.

## KISS Compliance

- [ ] One obvious way to make a text request.
- [ ] One obvious way to upload a file.
- [ ] One obvious way to download a file.
- [ ] One obvious way to stream a response.
- [ ] One obvious way to send multipart.
- [ ] No DSL explosion.
- [ ] No annotations.
- [ ] No hidden behavior.
- [ ] Method names are verbs, class names are nouns.
- [ ] API is guessable without reading docs.

## Dependency Check

- [ ] Zero production dependencies.
- [ ] JUnit 5 only in test scope.
- [ ] Maven build and release plugins are acceptable.
- [ ] No shaded or repackaged dependencies.

## Java 17

- [ ] Source and target compatibility set to 17.
- [ ] No preview features used.
- [ ] No Java 21+ APIs used in production code.
- [ ] No virtual threads API in production code (only via user-provided Executor).

## Maven Central Readiness

- [ ] groupId: `io.github.arthurhoch`
- [ ] artifactId: `kiss-requests`
- [ ] Version follows semver.
- [ ] Source JAR plugin configured.
- [ ] Javadoc JAR plugin configured.
- [ ] GPG plugin configured in `release` profile.
- [ ] Central publishing plugin configured in `release` profile.
- [ ] All required metadata present: name, description, url, licenses, developers, scm.
- [ ] `settings.xml` server ID matches `publishingServerId`.

## Documentation Readiness

- [ ] README.md is the quick start.
- [ ] docs/index.md links to all docs.
- [ ] Every public API has at least one example.
- [ ] Examples are copyable.
- [ ] GETTING_STARTED.md covers the basics.
- [ ] API.md documents all public methods.
- [ ] ERROR_HANDLING.md covers all error categories.
- [ ] CURL_DEBUGGING.md covers all request types.
- [ ] FILE_UPLOAD_DOWNLOAD.md covers upload, download, stream, multipart.
- [ ] CONFIGURATION.md covers all config options.
- [ ] All docs are in English.
- [ ] All code examples use English names.

## CI Readiness

- [ ] ci.yml runs on push and PR.
- [ ] ci.yml uses Java 17.
- [ ] ci.yml runs `mvn -B verify`.
- [ ] ci.yml caches Maven dependencies.
- [ ] ci.yml does not require publishing secrets.

## Release Workflow Readiness

- [ ] release-maven-central.yml triggers on `v*` tags.
- [ ] release-maven-central.yml runs tests before publishing.
- [ ] release-maven-central.yml uses Java 17.
- [ ] release-maven-central.yml imports GPG key.
- [ ] release-maven-central.yml runs `mvn -B deploy -P release`.
- [ ] Workflow comments document what manual setup is required.

## Test Coverage Plan

- [ ] Text request tests: GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS.
- [ ] Upload tests: small file, custom content-type, missing file.
- [ ] Download tests: file download, bytesWritten, missing directory.
- [ ] Stream tests: InputStream reading, stream closing.
- [ ] Multipart tests: text fields, files, mixed, missing file.
- [ ] toCurl tests: all request types.
- [ ] Retry tests: status codes, methods, backoff, max attempts.
- [ ] Timeout tests: connect timeout, request timeout.
- [ ] Error tests: rich exception fields, response headers, truncation.
- [ ] Concurrency tests: limit enforcement.
- [ ] Invalid input tests: null, empty, invalid values.

## Naming Review

- [ ] Class names are clear nouns: `Http`, `HttpCall`, `HttpResult`, `HttpException`.
- [ ] Method names are clear verbs: `execute`, `toCurl`, `request`, `upload`.
- [ ] Constants are clear: `HttpMethod.GET`, `HttpMethod.POST`.
- [ ] No abbreviations needing explanation.
- [ ] Package names follow convention: `io.github.arthurhoch.kissrequests`.

## Package Review

- [ ] Base package: `io.github.arthurhoch.kissrequests`.
- [ ] Internal: `io.github.arthurhoch.kissrequests.internal`.
- [ ] Internal implementation classes are not documented or supported as user API.
- [ ] Public API is in the base package.
- [ ] No extra public subpackages are introduced unless they simplify the API.
