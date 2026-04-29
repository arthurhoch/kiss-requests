package io.github.arthurhoch.kissrequests;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Result of a request downloaded directly to a file.
 *
 * @param statusCode HTTP response status code
 * @param headers response headers
 * @param file target file path
 * @param bytesWritten number of bytes written to {@code file}
 * @param duration total execution duration
 * @param attempts attempts made for this request
 * @param method HTTP method
 * @param url request URL
 */
public record HttpDownloadResult(
        int statusCode,
        Map<String, List<String>> headers,
        Path file,
        long bytesWritten,
        Duration duration,
        List<HttpAttempt> attempts,
        String method,
        String url
) {}
