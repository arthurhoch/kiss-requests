package io.github.arthurhoch.kissrequests;

import io.github.arthurhoch.kissrequests.internal.CallType;
import io.github.arthurhoch.kissrequests.internal.CurlRenderer;
import io.github.arthurhoch.kissrequests.internal.HttpExecutionEngine;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

/**
 * Immutable prepared HTTP call.
 *
 * <p>A call is not sent when it is created. Use {@link #toCurl()} or
 * {@link #toCurlBase64()} for debugging, and {@link #execute()} to send it.</p>
 *
 * @param <T> result type returned by {@link #execute()}
 */
public final class HttpCall<T> {
    private final HttpExecutionEngine engine;
    private final CallType callType;
    private final String method;
    private final String url;
    private final Map<String, String> headers;
    private final String body;
    private final Path file;
    private final Path targetPath;
    private final Map<String, String> fields;
    private final Map<String, Path> fileFields;

    HttpCall(HttpExecutionEngine engine, CallType callType,
             String method, String url, Map<String, String> headers,
             String body, Path file, Path targetPath,
             Map<String, String> fields, Map<String, Path> fileFields) {
        this.engine = engine;
        this.callType = callType;
        this.method = method;
        this.url = url;
        this.headers = headers != null ? Map.copyOf(headers) : Map.of();
        this.body = body;
        this.file = file;
        this.targetPath = targetPath;
        this.fields = fields != null ? Map.copyOf(fields) : Map.of();
        this.fileFields = fileFields != null ? Map.copyOf(fileFields) : Map.of();
    }

    /**
     * Sends the prepared request and returns its result.
     *
     * @return result for this call type
     * @throws HttpException when execution fails or returns a rejected status
     */
    @SuppressWarnings("unchecked")
    public T execute() {
        return (T) engine.execute(this);
    }

    /**
     * Renders the prepared call as a copyable curl command without executing it.
     *
     * @return curl command
     */
    public String toCurl() {
        return CurlRenderer.render(this);
    }

    /**
     * Renders the prepared call as Base64 of {@link #toCurl()} without executing it.
     *
     * @return Base64-encoded curl command
     */
    public String toCurlBase64() {
        return Base64.getEncoder().encodeToString(toCurl().getBytes(StandardCharsets.UTF_8));
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
     * Returns immutable request headers.
     *
     * @return immutable request headers
     */
    public Map<String, String> headers() { return headers; }
    /**
     * Returns the text body.
     *
     * @return text body, or {@code null}
     */
    public String body() { return body; }
    /**
     * Returns the upload file path.
     *
     * @return upload file path, or {@code null}
     */
    public Path file() { return file; }
    /**
     * Returns the download target path.
     *
     * @return download target path, or {@code null}
     */
    public Path targetPath() { return targetPath; }
    /**
     * Returns immutable multipart text fields.
     *
     * @return immutable multipart text fields
     */
    public Map<String, String> fields() { return fields; }
    /**
     * Returns immutable multipart file fields.
     *
     * @return immutable multipart file fields
     */
    public Map<String, Path> fileFields() { return fileFields; }

    /**
     * Returns the call type used by the execution engine.
     *
     * @return call type used by the execution engine
     */
    public CallType callType() { return callType; }
}
