# 02 — KISS Rules

This document defines the KISS (Keep It Simple, Stupid) design rules for KissRequests.

Every design decision must pass these rules. If a proposed feature violates any rule, it must be rejected or redesigned.

## One Obvious Way

### One obvious way to make text requests

```java
Http http = Http.create();
HttpResult result = http.request(HttpMethod.GET, "https://api.example.com/users").execute();
```

Not two ways. Not three ways. One way.

### One obvious way to upload files

```java
Http http = Http.create();
HttpResult result = http.upload(HttpMethod.POST, "https://api.example.com/files",
        Map.of("Content-Type", "application/octet-stream"),
        Path.of("document.pdf")).execute();
```

### One obvious way to download files

```java
Http http = Http.create();
HttpDownloadResult result = http.download(HttpMethod.GET, "https://api.example.com/files/123",
        Map.of(),
        Path.of("output.pdf")).execute();
```

### One obvious way to stream responses

```java
Http http = Http.create();
HttpStreamResult result = http.stream(HttpMethod.GET, "https://api.example.com/stream",
        Map.of(), null).execute();
try (InputStream is = result.inputStream()) {
    // read the stream
}
```

### One obvious way to send multipart

```java
Http http = Http.create();
HttpResult result = http.multipart(HttpMethod.POST, "https://api.example.com/upload",
        Map.of(),
        Map.of("name", "Arthur"),
        Map.of("file", Path.of("photo.jpg"))).execute();
```

## No DSL Explosion

1. Do not create a fluent builder chain for every combination of options.
2. Do not create method overloads for every parameter variation beyond what is listed in the API contract.
3. The API has five operations: `request`, `upload`, `download`, `stream`, `multipart`. Each has a small number of overloads.
4. Configuration is on the `Http` singleton, not on individual calls.

## No Annotations

1. No annotations for configuration. Use the builder pattern.
2. No annotations for serialization. The library does not serialize.
3. No annotations for routing. The library is not a REST framework.

## No Magic Object Mapping

1. Do not automatically map response bodies to objects.
2. Do not use reflection to inspect user types.
3. The user gets a `String body()` or an `InputStream`. What they do with it is their business.

## No Hidden Framework Behavior

1. No automatic retry on every status code. Retry is explicit and configurable.
2. No automatic redirect following. The JDK `HttpClient` default redirect policy is used, and KissRequests does not expose redirect configuration in v1.
3. No automatic cookie management.
4. No automatic decompression beyond what the JDK provides.
5. No thread pool creation unless the user configures one.

## No Dependency Creep

1. Zero mandatory external dependencies in the core library.
2. No "optional" dependencies that are effectively required for common use.
3. No shading or repackaging of third-party libraries.
4. JUnit 5 for tests only.

## Public API Must Be Memorable

1. Class names: `Http`, `HttpCall`, `HttpResult`, `HttpException`. Obvious names.
2. Method names: `request`, `execute`, `toCurl`. Verbs that do what they say.
3. No abbreviations that need explanation. No acronyms except HTTP.
4. No factory patterns that require understanding of implementation details.
5. A Java developer should be able to guess the API without reading docs.

## Enforcement

When reviewing a proposed change, ask:

1. Is there already a way to do this? If yes, do not add another.
2. Does this require the user to learn a new concept? If yes, reject unless essential.
3. Does this add a dependency? If yes, reject without explicit human approval.
4. Does this make the library harder to memorize? If yes, reject.
5. Is this solving a real user need or a hypothetical one? If hypothetical, defer.
