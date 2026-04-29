# 00 — Product Purpose

## Why KissRequests Exists

Java developers need to make HTTP requests. The JDK provides `java.net.http.HttpClient` since Java 11, but its API is verbose and error-prone for common cases. Third-party libraries like Apache HttpClient and OkHttp are powerful but heavy, pulling in transitive dependencies and complex configurations.

KissRequests fills the gap: a tiny library that makes the common case trivial, stays out of the way, and uses only what the JDK already provides.

## What Pain It Solves

1. **Verbosity.** Making a simple GET with JDK HttpClient requires boilerplate for request building, response handling, and error management. KissRequests reduces this to one memorable line.
2. **Debugging difficulty.** When an HTTP call fails, developers need to see what was sent. KissRequests provides `toCurl()` on every prepared call so the exact request can be copied and replayed.
3. **Poor error messages.** JDK HttpClient throws generic exceptions. KissRequests throws `HttpException` with method, URL, curl, attempts, duration, status code, response body, and root cause.
4. **Dependency bloat.** Other libraries bring transitive dependency trees. KissRequests has zero mandatory external dependencies.
5. **Configuration complexity.** Other libraries require builders, pools, and setup. KissRequests works with `Http.create()` and sensible defaults.

## Why Native JDK HttpClient

1. It is already in the JDK. No additional jar on the classpath.
2. It supports HTTP/1.1 and HTTP/2.
3. It supports synchronous and asynchronous requests.
4. It supports request/response body publishers and handlers for streaming.
5. It is maintained by the OpenJDK team.
6. KissRequests wraps it with a simpler, more memorable API.

## Why the API Must Be Minimal

1. A memorable API is a usable API. If the user has to read documentation to make a GET request, the API has failed.
2. Every public method must earn its place. If there are two ways to do the same thing, one should be removed.
3. The library must not become a framework. No annotations, no IoC, no plugin system, no configuration files.
4. The library must not become a REST client. It handles HTTP requests. Serialization, routing, and service discovery are out of scope.

## Target Users

1. Java developers who need to make HTTP requests in application code, scripts, or tools.
2. Developers who want zero external dependencies.
3. Developers who value simplicity and debuggability over feature richness.
4. Developers who are frustrated by verbose HTTP client APIs.
5. Developers building libraries or CLIs who need a lightweight HTTP transport.

## What "Easy Enough to Use Without Documentation" Means

1. `Http.create()` is the starting point. No configuration required.
2. `http.request(method, url).execute()` is the simplest call. Obvious from the method names.
3. `call.toCurl()` is discoverable. It does what it says.
4. Error messages are self-explanatory. The exception contains everything needed to debug.
5. Method names are verbs. Class names are nouns. No surprises.
6. The API has one obvious way to do each thing: text request, upload, download, stream, multipart.
