package io.github.arthurhoch.kissrequests;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Immutable configuration used by an {@link Http} instance.
 *
 * @param connectTimeout timeout for establishing connections
 * @param requestTimeout timeout applied to each request
 * @param retryPolicy retry policy used by the execution engine
 * @param maxConcurrentRequests maximum concurrent requests, or {@code 0} for unlimited
 * @param executor optional executor for the underlying JDK {@code HttpClient}
 */
public record HttpConfig(
        Duration connectTimeout,
        Duration requestTimeout,
        RetryPolicy retryPolicy,
        int maxConcurrentRequests,
        Executor executor
) {
    /**
     * Validates timeout and concurrency values.
     */
    public HttpConfig {
        Objects.requireNonNull(connectTimeout, "connectTimeout must not be null");
        Objects.requireNonNull(requestTimeout, "requestTimeout must not be null");
        Objects.requireNonNull(retryPolicy, "retryPolicy must not be null");
        if (connectTimeout.isZero() || connectTimeout.isNegative()) {
            throw new IllegalArgumentException("connectTimeout must be positive");
        }
        if (requestTimeout.isZero() || requestTimeout.isNegative()) {
            throw new IllegalArgumentException("requestTimeout must be positive");
        }
        if (maxConcurrentRequests < 0) {
            throw new IllegalArgumentException("maxConcurrentRequests must be 0 or greater");
        }
    }

    /**
     * Creates the default configuration.
     *
     * @return default config: 10s connect timeout, 30s request timeout, no retries, unlimited concurrency
     */
    public static HttpConfig defaults() {
        return new HttpConfig(
                Duration.ofSeconds(10),
                Duration.ofSeconds(30),
                RetryPolicy.defaults(),
                0,
                null
        );
    }
}
