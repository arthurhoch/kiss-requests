# KissRequests Caveman Summary

## What this is

Java 17+ HTTP library. Uses native java.net.http.HttpClient. No production dependencies. Not a framework. Makes HTTP requests easy.

For consumer-project AI instructions, use docs/KISSREQUESTS_AI_USAGE.md.

## Main mental model

    HttpResult result = http.request(
            HttpMethod.POST,
            url,
            headers,
            body
    ).execute();

- http.request(...) prepares the call. No network activity.
- .execute() sends the network request.
- .toCurl() shows the request as curl before execution.

## V1 must support

- text request
- binary file upload
- file download
- stream response as InputStream
- multipart/form-data
- toCurl
- execute
- rich errors
- retry
- timeout
- max concurrent requests
- config in singleton

## KISS rules

- Keep API tiny.
- Do not add post/get helper methods unless explicitly approved.
- Do not add annotations.
- Do not add JSON mapping.
- Do not add XML mapping.
- Do not add framework integrations.
- Do not add secret masking.
- Do not add OAuth helpers.
- Do not add telemetry.
- Do not add cache.
- Do not add circuit breaker.
- Do not add service discovery.
- Do not add production dependencies.
- Do not make users read a manual to send one request.

## Public API shape

    http.request(method, url, headers, body).execute();
    http.upload(method, url, headers, path).execute();
    http.download(method, url, headers, targetPath).execute();
    http.stream(method, url, headers, body).execute();
    http.multipart(method, url, headers, fields, files).execute();

## Error rule

All execution failures must throw one rich library exception. It must include method, URL, curl, attempts, duration, status if available, response body if available, and cause if available. Retry history must be visible.

## Curl rule

Every prepared call must support toCurl(). toCurlBase64() returns the same curl command Base64-encoded for logs/copy-paste. Not encryption. No secret masking in v1. Keep output simple and useful.

## Config rule

Http.create() must work with safe defaults. Http.builder() is for advanced config. Config belongs to singleton/client, not to the common request call. Java 17 core must remain compatible. Virtual threads may be used only through user-provided Executor or future optional Java 21 support.

## Testing rule

Tests must not use internet. Tests must use local server or pure unit tests. Cover all public behavior. No flaky tests.

## Documentation rule

If public behavior changes, update docs. If public API changes, update examples. Keep examples copyable. Keep docs short where possible.

## Security

- No production dependencies. Attack surface is minimal by design.
- CodeQL and Dependabot scan the repository automatically in GitHub.
- OWASP Dependency-Check runs with `mvn -Psecurity verify` only. Not in normal builds.
- Do not commit secrets. See docs/SECRET_HYGIENE.md.
- `.toCurl()` does not mask secrets in v1. Applications must protect their own logs.
- Report vulnerabilities to SECURITY.md.

## Before coding

Read these files in order:

1. CAVEMAN.md (this file — compact summary)
2. AGENTS.md (authoritative rules)
3. docs/PRODUCT_SPEC.md (full spec)
4. .github/architecture/index.md (architecture reading order)
5. docs/IMPLEMENTATION_PLAN.md (implementation phases)

CAVEMAN.md is a summary only. If it conflicts with detailed docs, the detailed docs win.
