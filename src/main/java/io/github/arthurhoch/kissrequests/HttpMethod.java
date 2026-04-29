package io.github.arthurhoch.kissrequests;

/**
 * Common HTTP method constants.
 *
 * <p>This is intentionally not an enum. Request methods are accepted as strings
 * so uncommon or custom methods can still be used.</p>
 */
public final class HttpMethod {
    /** HTTP GET. */
    public static final String GET = "GET";
    /** HTTP POST. */
    public static final String POST = "POST";
    /** HTTP PUT. */
    public static final String PUT = "PUT";
    /** HTTP DELETE. */
    public static final String DELETE = "DELETE";
    /** HTTP PATCH. */
    public static final String PATCH = "PATCH";
    /** HTTP HEAD. */
    public static final String HEAD = "HEAD";
    /** HTTP OPTIONS. */
    public static final String OPTIONS = "OPTIONS";

    private HttpMethod() {}
}
