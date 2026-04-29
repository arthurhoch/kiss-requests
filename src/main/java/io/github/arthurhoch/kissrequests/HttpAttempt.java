package io.github.arthurhoch.kissrequests;

import java.time.Duration;

/**
 * Information about one execution attempt.
 *
 * @param attemptNumber one-based attempt number
 * @param statusCode HTTP status code, or {@code -1} when no response was received
 * @param duration duration of this attempt
 * @param failureMessage failure message, or {@code null} when the attempt received a response
 */
public record HttpAttempt(
        int attemptNumber,
        int statusCode,
        Duration duration,
        String failureMessage
) {}
