package io.github.arthurhoch.kissrequests;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable retry policy for an {@link Http} instance.
 *
 * <p>{@link #defaults()} performs one attempt only. The {@code of(...)}
 * factories enable conservative retries for common idempotent methods and
 * transient HTTP status codes.</p>
 */
public final class RetryPolicy {
    private final int maxAttempts;
    private final Duration initialBackoff;
    private final Set<Integer> retryOnStatusCodes;
    private final Set<String> retryOnMethods;

    private static final Set<Integer> DEFAULT_RETRY_STATUS_CODES = Set.of(429, 500, 502, 503, 504);
    private static final Set<String> DEFAULT_RETRY_METHODS = Set.of("GET", "HEAD", "OPTIONS", "PUT", "DELETE");

    private RetryPolicy(int maxAttempts, Duration initialBackoff,
                        Set<Integer> retryOnStatusCodes, Set<String> retryOnMethods) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        this.maxAttempts = maxAttempts;
        this.initialBackoff = Objects.requireNonNull(initialBackoff, "initialBackoff must not be null");
        this.retryOnStatusCodes = Set.copyOf(Objects.requireNonNull(retryOnStatusCodes, "retryOnStatusCodes must not be null"));
        this.retryOnMethods = normalizeMethods(retryOnMethods);
    }

    /**
     * Creates the default policy: one attempt and no retries.
     *
     * @return default retry policy
     */
    public static RetryPolicy defaults() {
        return new RetryPolicy(1, Duration.ZERO, Set.of(), Set.of());
    }

    /**
     * Creates a conservative retry policy using default backoff, status codes, and methods.
     *
     * @param maxAttempts maximum total attempts, including the first try
     * @return retry policy
     */
    public static RetryPolicy of(int maxAttempts) {
        return new RetryPolicy(maxAttempts, Duration.ofMillis(500), DEFAULT_RETRY_STATUS_CODES, DEFAULT_RETRY_METHODS);
    }

    /**
     * Creates a conservative retry policy using custom initial backoff.
     *
     * @param maxAttempts maximum total attempts, including the first try
     * @param initialBackoff initial retry backoff
     * @return retry policy
     */
    public static RetryPolicy of(int maxAttempts, Duration initialBackoff) {
        return new RetryPolicy(maxAttempts, initialBackoff, DEFAULT_RETRY_STATUS_CODES, DEFAULT_RETRY_METHODS);
    }

    /**
     * Creates a retry policy with explicit retry status codes and methods.
     *
     * @param maxAttempts maximum total attempts, including the first try
     * @param initialBackoff initial retry backoff
     * @param retryOnStatusCodes status codes that may be retried
     * @param retryOnMethods methods that may be retried
     * @return retry policy
     */
    public static RetryPolicy of(int maxAttempts, Duration initialBackoff,
                                 Set<Integer> retryOnStatusCodes, Set<String> retryOnMethods) {
        return new RetryPolicy(maxAttempts, initialBackoff, retryOnStatusCodes, retryOnMethods);
    }

    /**
     * Returns the maximum total attempts.
     *
     * @return maximum total attempts, including the first try
     */
    public int maxAttempts() {
        return maxAttempts;
    }

    /**
     * Returns the initial retry backoff.
     *
     * @return initial retry backoff
     */
    public Duration initialBackoff() {
        return initialBackoff;
    }

    /**
     * Returns immutable retryable status codes.
     *
     * @return immutable set of retryable status codes
     */
    public Set<Integer> retryOnStatusCodes() {
        return retryOnStatusCodes;
    }

    /**
     * Returns immutable retryable methods.
     *
     * @return immutable set of retryable methods
     */
    public Set<String> retryOnMethods() {
        return retryOnMethods;
    }

    /**
     * Checks whether a status code is configured as retryable.
     *
     * @param statusCode response status code
     * @return true if this status code is configured as retryable
     */
    public boolean shouldRetryOnStatus(int statusCode) {
        return retryOnStatusCodes.contains(statusCode);
    }

    /**
     * Checks whether a method is configured as retryable.
     *
     * @param method HTTP method
     * @return true if this method is configured as retryable
     */
    public boolean shouldRetryMethod(String method) {
        return method != null && retryOnMethods.contains(method.toUpperCase(Locale.ROOT));
    }

    private static Set<String> normalizeMethods(Set<String> methods) {
        Objects.requireNonNull(methods, "retryOnMethods must not be null");

        Set<String> normalized = new LinkedHashSet<>();
        for (String method : methods) {
            if (method == null || method.isBlank()) {
                throw new IllegalArgumentException("retry methods must not contain null or blank values");
            }
            normalized.add(method.toUpperCase(Locale.ROOT));
        }
        return Set.copyOf(normalized);
    }
}
