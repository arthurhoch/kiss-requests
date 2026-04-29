package io.github.arthurhoch.kissrequests;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rich exception thrown when request execution fails or returns a rejected status.
 */
public class HttpException extends RuntimeException {
    private static final int MAX_RESPONSE_BODY_SIZE = 4096;

    private final String method;
    private final String url;
    private final String curl;
    private final List<HttpAttempt> attempts;
    private final Duration totalDuration;
    private final int statusCode;
    private final Map<String, List<String>> responseHeaders;
    private final String responseBody;
    private final Throwable rootCause;

    /**
     * Creates an exception with all debugging fields.
     *
     * @param method HTTP method
     * @param url request URL
     * @param curl rendered curl command
     * @param attempts execution attempts
     * @param totalDuration total duration across attempts
     * @param statusCode response status code, or {@code -1} when unavailable
     * @param responseHeaders response headers, if available
     * @param responseBody response body, if available
     * @param rootCause root cause, if available
     */
    public HttpException(String method, String url, String curl,
                         List<HttpAttempt> attempts, Duration totalDuration,
                         int statusCode, Map<String, List<String>> responseHeaders,
                         String responseBody, Throwable rootCause) {
        super(buildMessage(method, url, statusCode, rootCause, attempts, totalDuration));
        this.method = method;
        this.url = url;
        this.curl = curl;
        this.attempts = List.copyOf(attempts);
        this.totalDuration = totalDuration;
        this.statusCode = statusCode;
        this.responseHeaders = copyHeaders(responseHeaders);
        this.responseBody = responseBody;
        this.rootCause = rootCause;
    }

    /**
     * Returns the HTTP method.
     *
     * @return HTTP method
     */
    public String method() { return method; }
    /**
     * Returns the request URL.
     *
     * @return request URL
     */
    public String url() { return url; }
    /**
     * Returns the rendered curl command.
     *
     * @return rendered curl command
     */
    public String curl() { return curl; }
    /**
     * Returns immutable execution attempts.
     *
     * @return immutable execution attempts
     */
    public List<HttpAttempt> attempts() { return attempts; }
    /**
     * Returns the total duration across attempts.
     *
     * @return total duration across attempts
     */
    public Duration totalDuration() { return totalDuration; }
    /**
     * Returns the response status code.
     *
     * @return response status code, or {@code -1} when unavailable
     */
    public int statusCode() { return statusCode; }
    /**
     * Returns immutable response headers.
     *
     * @return immutable response headers, or empty map when unavailable
     */
    public Map<String, List<String>> responseHeaders() { return responseHeaders; }
    /**
     * Returns the response body captured for diagnostics.
     *
     * @return response body, possibly truncated, or {@code null} when unavailable
     */
    public String responseBody() { return responseBody; }

    /**
     * Returns the root cause.
     *
     * @return root cause, or {@code null} when unavailable
     */
    @Override
    public Throwable getCause() { return rootCause; }

    /**
     * Returns the root cause.
     *
     * @return root cause, or {@code null} when unavailable
     */
    public Throwable rootCause() { return rootCause; }

    /**
     * Builds a human-readable diagnostic report.
     *
     * @return report containing message, attempts, curl, response data, and cause
     */
    public String report() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMessage());
        sb.append('\n');

        for (HttpAttempt attempt : attempts) {
            sb.append("  Attempt ").append(attempt.attemptNumber());
            sb.append(": status=").append(attempt.statusCode());
            sb.append(", duration=").append(attempt.duration().toMillis()).append("ms");
            if (attempt.failureMessage() != null) {
                sb.append(" - ").append(attempt.failureMessage());
            }
            sb.append('\n');
        }

        sb.append("  Curl: ").append(curl);

        if (responseBody != null) {
            sb.append("\n  Response: ").append(responseBody);
        }

        if (!responseHeaders.isEmpty()) {
            sb.append("\n  Response headers: ").append(responseHeaders);
        }

        if (rootCause != null) {
            sb.append("\n  Cause: ").append(rootCause.getClass().getSimpleName());
            sb.append(": ").append(rootCause.getMessage());
        }

        return sb.toString();
    }

    /**
     * Truncates a response body to the maximum size stored in exceptions.
     *
     * @param body response body, or {@code null}
     * @return original body or truncated body
     */
    public static String truncateBody(String body) {
        if (body == null) return null;
        if (body.length() <= MAX_RESPONSE_BODY_SIZE) return body;
        return body.substring(0, MAX_RESPONSE_BODY_SIZE) + "\n... [truncated]";
    }

    private static Map<String, List<String>> copyHeaders(Map<String, List<String>> headers) {
        if (headers == null || headers.isEmpty()) return Map.of();

        Map<String, List<String>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(copy);
    }

    private static String buildMessage(String method, String url, int statusCode,
                                       Throwable rootCause, List<HttpAttempt> attempts,
                                       Duration totalDuration) {
        int attemptCount = attempts != null ? attempts.size() : 0;
        long millis = totalDuration != null ? totalDuration.toMillis() : 0;

        if (statusCode > 0) {
            return String.format("HTTP %d for %s %s (%d attempt%s, %dms)",
                    statusCode, method, url, attemptCount,
                    attemptCount != 1 ? "s" : "", millis);
        }

        String causeMsg = rootCause != null ? rootCause.getMessage() : "unknown error";
        return String.format("HTTP request failed for %s %s: %s (%d attempt%s, %dms)",
                method, url, causeMsg, attemptCount,
                attemptCount != 1 ? "s" : "", millis);
    }
}
