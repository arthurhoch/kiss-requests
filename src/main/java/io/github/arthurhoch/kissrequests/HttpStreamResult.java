package io.github.arthurhoch.kissrequests;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Result of a request whose response body is returned as a stream.
 *
 * <p>The caller owns the returned {@link InputStream} and must close it.</p>
 *
 * @param statusCode HTTP response status code
 * @param headers response headers
 * @param inputStream response body stream
 * @param duration total execution duration
 * @param attempts attempts made for this request
 * @param method HTTP method
 * @param url request URL
 */
public record HttpStreamResult(
        int statusCode,
        Map<String, List<String>> headers,
        InputStream inputStream,
        Duration duration,
        List<HttpAttempt> attempts,
        String method,
        String url
) {}
