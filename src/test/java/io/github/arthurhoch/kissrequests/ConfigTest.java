package io.github.arthurhoch.kissrequests;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {
    @Test
    void shouldCreateWithDefaults() {
        Http http = Http.create();
        assertNotNull(http);

        String curl = http.request("GET", "https://httpbin.org/get").toCurl();
        assertNotNull(curl);
    }

    @Test
    void shouldConfigureWithBuilder() {
        Http http = Http.builder()
                .connectTimeout(Duration.ofSeconds(5))
                .requestTimeout(Duration.ofSeconds(10))
                .retryPolicy(RetryPolicy.of(3, Duration.ofMillis(100)))
                .build();
        assertNotNull(http);
    }

    @Test
    void shouldConfigureMaxConcurrentRequests() {
        Http http = Http.builder()
                .maxConcurrentRequests(2)
                .build();
        assertNotNull(http);
    }

    @Test
    void shouldConfigureExecutor() {
        Http http = Http.builder()
                .executor(Executors.newFixedThreadPool(4))
                .build();
        assertNotNull(http);
    }

    @Test
    void retryPolicyDefaultsShouldHaveOneAttempt() {
        RetryPolicy policy = RetryPolicy.defaults();
        assertEquals(1, policy.maxAttempts());
    }

    @Test
    void retryPolicyOfShouldHaveCorrectMaxAttempts() {
        RetryPolicy policy = RetryPolicy.of(5);
        assertEquals(5, policy.maxAttempts());
        assertEquals(Duration.ofMillis(500), policy.initialBackoff());
        assertTrue(policy.retryOnStatusCodes().contains(500));
        assertTrue(policy.retryOnMethods().contains("GET"));
        assertFalse(policy.retryOnMethods().contains("POST"));
    }

    @Test
    void retryPolicyShouldAllowCustomStatusCodesAndMethods() {
        RetryPolicy policy = RetryPolicy.of(2, Duration.ZERO, Set.of(418), Set.of("post"));

        assertTrue(policy.retryOnStatusCodes().contains(418));
        assertTrue(policy.retryOnMethods().contains("POST"));
        assertTrue(policy.shouldRetryMethod("POST"));
        assertTrue(policy.shouldRetryMethod("post"));
        assertFalse(policy.shouldRetryOnStatus(500));
    }

    @Test
    void retryPolicyShouldRejectInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(0));
    }

    @Test
    void shouldRejectNullConnectTimeout() {
        assertThrows(NullPointerException.class, () ->
                Http.builder().connectTimeout(null).build());
    }

    @Test
    void shouldRejectNonPositiveRequestTimeout() {
        assertThrows(IllegalArgumentException.class, () ->
                Http.builder().requestTimeout(Duration.ZERO).build());
    }

    @Test
    void shouldRejectNegativeMaxConcurrentRequests() {
        assertThrows(IllegalArgumentException.class, () ->
                Http.builder().maxConcurrentRequests(-1).build());
    }

    @Test
    void shouldRejectNullRetryPolicy() {
        assertThrows(NullPointerException.class, () ->
                Http.builder().retryPolicy(null).build());
    }

    @Test
    void httpConfigDefaultsShouldHaveSafeValues() {
        HttpConfig config = HttpConfig.defaults();
        assertEquals(Duration.ofSeconds(10), config.connectTimeout());
        assertEquals(Duration.ofSeconds(30), config.requestTimeout());
        assertEquals(0, config.maxConcurrentRequests());
        assertNull(config.executor());
    }
}
