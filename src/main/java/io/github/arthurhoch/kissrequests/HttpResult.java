package io.github.arthurhoch.kissrequests;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Result of a text, upload, or multipart request.
 *
 * @param statusCode HTTP response status code
 * @param headers response headers
 * @param body response body decoded as UTF-8 text
 * @param duration total execution duration
 * @param attempts attempts made for this request
 * @param method HTTP method
 * @param url request URL
 */
public record HttpResult(
        int statusCode,
        Map<String, List<String>> headers,
        String body,
        Duration duration,
        List<HttpAttempt> attempts,
        String method,
        String url
) {}
