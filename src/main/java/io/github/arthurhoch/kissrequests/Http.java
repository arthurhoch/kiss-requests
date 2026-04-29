package io.github.arthurhoch.kissrequests;

import io.github.arthurhoch.kissrequests.internal.CallType;
import io.github.arthurhoch.kissrequests.internal.HttpExecutionEngine;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Main entry point for preparing and executing HTTP calls.
 *
 * <p>An {@code Http} instance is immutable after construction and can be reused
 * across threads. Use {@link #create()} for defaults or {@link #builder()} for
 * shared integration-specific configuration.</p>
 */
public final class Http {
    private final HttpConfig config;
    private final HttpClient httpClient;
    private final HttpExecutionEngine engine;

    private Http(HttpConfig config) {
        this.config = config;

        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(config.connectTimeout());

        if (config.executor() != null) {
            builder.executor(config.executor());
        }

        this.httpClient = builder.build();
        this.engine = new HttpExecutionEngine(httpClient, config);
    }

    /**
     * Creates a reusable instance with default timeout, retry, concurrency, and executor settings.
     *
     * @return a configured {@code Http} instance
     */
    public static Http create() {
        return new Http(HttpConfig.defaults());
    }

    /**
     * Starts a builder for advanced shared configuration.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Prepares a text request without headers or body.
     *
     * @param method HTTP method, usually from {@link HttpMethod}
     * @param url request URL
     * @return prepared call; no network request is sent until {@link HttpCall#execute()}
     */
    public HttpCall<HttpResult> request(String method, String url) {
        return request(method, url, Map.of(), null);
    }

    /**
     * Prepares a text request with headers and no body.
     *
     * @param method HTTP method, usually from {@link HttpMethod}
     * @param url request URL
     * @param headers request headers; {@code null} is treated as empty
     * @return prepared call; no network request is sent until {@link HttpCall#execute()}
     */
    public HttpCall<HttpResult> request(String method, String url, Map<String, String> headers) {
        return request(method, url, headers, null);
    }

    /**
     * Prepares a text request.
     *
     * @param method HTTP method, usually from {@link HttpMethod}
     * @param url request URL
     * @param headers request headers; {@code null} is treated as empty
     * @param body UTF-8 text body, or {@code null} for no body
     * @return prepared call; no network request is sent until {@link HttpCall#execute()}
     */
    public HttpCall<HttpResult> request(String method, String url, Map<String, String> headers, String body) {
        validate(method, url);
        return new HttpCall<>(engine, CallType.TEXT, method, url,
                safeHeaders(headers), body, null, null, null, null);
    }

    /**
     * Prepares a binary file upload request.
     *
     * @param method HTTP method, usually {@link HttpMethod#POST} or {@link HttpMethod#PUT}
     * @param url request URL
     * @param headers request headers; {@code null} is treated as empty
     * @param file file path to stream from disk
     * @return prepared call; no network request is sent until {@link HttpCall#execute()}
     */
    public HttpCall<HttpResult> upload(String method, String url, Map<String, String> headers, Path file) {
        validate(method, url);
        Objects.requireNonNull(file, "file must not be null");
        return new HttpCall<>(engine, CallType.UPLOAD, method, url,
                safeHeaders(headers), null, file, null, null, null);
    }

    /**
     * Prepares a request whose response body is downloaded directly to a file.
     *
     * @param method HTTP method, usually {@link HttpMethod#GET}
     * @param url request URL
     * @param headers request headers; {@code null} is treated as empty
     * @param targetPath target file path
     * @return prepared call; no network request is sent until {@link HttpCall#execute()}
     */
    public HttpCall<HttpDownloadResult> download(String method, String url, Map<String, String> headers, Path targetPath) {
        validate(method, url);
        Objects.requireNonNull(targetPath, "targetPath must not be null");
        return new HttpCall<>(engine, CallType.DOWNLOAD, method, url,
                safeHeaders(headers), null, null, targetPath, null, null);
    }

    /**
     * Prepares a request whose response body is returned as an {@code InputStream}.
     *
     * @param method HTTP method, usually from {@link HttpMethod}
     * @param url request URL
     * @param headers request headers; {@code null} is treated as empty
     * @param body UTF-8 text body, or {@code null} for no body
     * @return prepared call; no network request is sent until {@link HttpCall#execute()}
     */
    public HttpCall<HttpStreamResult> stream(String method, String url, Map<String, String> headers, String body) {
        validate(method, url);
        return new HttpCall<>(engine, CallType.STREAM, method, url,
                safeHeaders(headers), body, null, null, null, null);
    }

    /**
     * Prepares a multipart/form-data request.
     *
     * @param method HTTP method, usually {@link HttpMethod#POST}
     * @param url request URL
     * @param headers request headers; {@code null} is treated as empty
     * @param fields text form fields; {@code null} is treated as empty
     * @param files file form fields; {@code null} is treated as empty
     * @return prepared call; no network request is sent until {@link HttpCall#execute()}
     */
    public HttpCall<HttpResult> multipart(String method, String url, Map<String, String> headers,
                                          Map<String, String> fields, Map<String, Path> files) {
        validate(method, url);
        return new HttpCall<>(engine, CallType.MULTIPART, method, url,
                safeHeaders(headers), null, null, null, fields, files);
    }

    private static void validate(String method, String url) {
        Objects.requireNonNull(method, "method must not be null");
        Objects.requireNonNull(url, "url must not be null");
        if (method.isBlank()) throw new IllegalArgumentException("method must not be blank");
        if (url.isBlank()) throw new IllegalArgumentException("url must not be blank");
    }

    private static Map<String, String> safeHeaders(Map<String, String> headers) {
        return headers != null ? headers : Map.of();
    }

    /**
     * Builder for immutable {@link Http} instances.
     */
    public static final class Builder {
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration requestTimeout = Duration.ofSeconds(30);
        private RetryPolicy retryPolicy = RetryPolicy.defaults();
        private int maxConcurrentRequests = 0;
        private Executor executor = null;

        /**
         * Creates a builder with default values.
         */
        public Builder() {
        }

        /**
         * Sets the connection establishment timeout.
         *
         * @param timeout positive timeout
         * @return this builder
         */
        public Builder connectTimeout(Duration timeout) {
            this.connectTimeout = timeout;
            return this;
        }

        /**
         * Sets the per-request timeout.
         *
         * @param timeout positive timeout
         * @return this builder
         */
        public Builder requestTimeout(Duration timeout) {
            this.requestTimeout = timeout;
            return this;
        }

        /**
         * Sets the retry policy.
         *
         * @param policy retry policy
         * @return this builder
         */
        public Builder retryPolicy(RetryPolicy policy) {
            this.retryPolicy = policy;
            return this;
        }

        /**
         * Sets the maximum concurrent requests for this {@code Http} instance.
         *
         * @param max maximum concurrent requests, or {@code 0} for unlimited
         * @return this builder
         */
        public Builder maxConcurrentRequests(int max) {
            this.maxConcurrentRequests = max;
            return this;
        }

        /**
         * Sets the executor used by the underlying JDK {@code HttpClient}.
         *
         * @param executor executor, or {@code null} for the JDK default
         * @return this builder
         */
        public Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Builds a reusable {@link Http} instance.
         *
         * @return configured {@code Http} instance
         */
        public Http build() {
            HttpConfig cfg = new HttpConfig(connectTimeout, requestTimeout, retryPolicy, maxConcurrentRequests, executor);
            return new Http(cfg);
        }
    }
}
