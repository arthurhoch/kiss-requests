---
layout: default
title: KissRequests
---

<section class="hero">
  <div>
    <p class="eyebrow">KISS Java Libraries</p>
    <h1>KissRequests</h1>
    <p class="lead">Tiny zero-dependency Java 17+ HTTP client library built on native <code>java.net.http.HttpClient</code>, with prepared calls, curl rendering, retries, and rich error reports.</p>
    <div class="meta-row">
      <span class="tag">Latest stable: 0.1.0</span>
      <span class="tag">Java 17+</span>
      <span class="tag">Apache-2.0</span>
    </div>
    <div class="actions">
      <a class="button" href="GETTING_STARTED.html">Getting Started</a>
      <a class="button secondary" href="API.html">API Reference</a>
      <a class="button secondary" href="https://github.com/arthurhoch/kiss-requests">GitHub</a>
    </div>
  </div>
  <div class="panel">
    <p class="panel-title">Maven</p>
<pre><code>&lt;dependency&gt;
  &lt;groupId&gt;io.github.arthurhoch&lt;/groupId&gt;
  &lt;artifactId&gt;kiss-requests&lt;/artifactId&gt;
  &lt;version&gt;0.1.0&lt;/version&gt;
&lt;/dependency&gt;</code></pre>
  </div>
</section>

<section class="section two-column">
  <div>
    <h2>Small Surface</h2>
    <p>KissRequests keeps request construction, execution, debugging, and error handling explicit. It avoids REST framework abstractions while still covering common HTTP workflows.</p>
  </div>
  <div class="panel">
    <p class="panel-title">Quick Example</p>
<pre><code>Http http = Http.create();
HttpResult result = http
    .request(HttpMethod.GET, "https://api.example.com/users")
    .execute();</code></pre>
  </div>
</section>

<section class="section">
  <h2>KISS Principles</h2>
  <div class="feature-grid">
    <article class="feature">
      <h3>Native JDK</h3>
      <p>The production library builds on Java HttpClient and does not ship external runtime dependencies.</p>
    </article>
    <article class="feature">
      <h3>Debuggable Calls</h3>
      <p>Prepared calls can be rendered as curl, including base64-safe variants for binary bodies.</p>
    </article>
    <article class="feature">
      <h3>Useful Failures</h3>
      <p>Exceptions include method, URL, attempts, timing, status, headers, body, curl, and cause details.</p>
    </article>
  </div>
</section>

<section class="section">
  <h2>Documentation</h2>
  <div class="doc-grid">
    <a href="GETTING_STARTED.html">Getting Started<span>Install and make the first request.</span></a>
    <a href="API.html">API Reference<span>Public HTTP API and result types.</span></a>
    <a href="skills/index.html">AI Skills<span>Versioned Markdown skill files for AI-assisted usage.</span></a>
    <a href="EXAMPLES.html">Examples<span>Copyable examples for common calls.</span></a>
    <a href="CONFIGURATION.html">Configuration<span>Timeouts, retries, concurrency, and executors.</span></a>
    <a href="CURL_DEBUGGING.html">Curl Debugging<span>Use toCurl for reproducible diagnostics.</span></a>
    <a href="FILE_UPLOAD_DOWNLOAD.html">Files And Streams<span>Upload, download, stream, and multipart usage.</span></a>
    <a href="ERROR_HANDLING.html">Error Handling<span>How rich exceptions are structured.</span></a>
    <a href="SECURITY_SCANNING.html">Security Scanning<span>CodeQL, Dependabot, Dependency Review, and OWASP.</span></a>
    <a href="security-hardening.html">Security Hardening<span>Repository hardening and local quality commands.</span></a>
    <a href="SECRET_HYGIENE.html">Secret Hygiene<span>Rules for credentials and curl output.</span></a>
    <a href="code-cleanup.html">Safe Code Cleanup<span>Deletion policy and quality gates.</span></a>
    <a href="RELEASE.html">Release<span>Release process and Maven Central flow.</span></a>
  </div>
</section>

<section class="section">
  <h2>Related Projects</h2>
  <div class="related-grid">
    <a href="https://github.com/arthurhoch/kiss-json">kiss-json<span>Field-based JSON serialization and deserialization.</span></a>
    <a href="https://github.com/arthurhoch/kiss-requests">kiss-requests<span>Simple HTTP client built on Java HttpClient.</span></a>
    <a href="https://github.com/arthurhoch/kiss-server">kiss-server<span>Small HTTP/1.1 server for simple REST-style applications.</span></a>
    <a href="https://github.com/arthurhoch/kiss-config">kiss-config<span>Configuration from properties, .env, system properties, and environment variables.</span></a>
    <a href="https://github.com/arthurhoch/kiss-binary">kiss-binary<span>Explicit binary IO for primitive binary formats.</span></a>
  </div>
</section>
