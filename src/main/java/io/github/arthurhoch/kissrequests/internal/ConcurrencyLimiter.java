package io.github.arthurhoch.kissrequests.internal;

import java.util.concurrent.Semaphore;

public final class ConcurrencyLimiter {
    private final Semaphore semaphore;

    public ConcurrencyLimiter(int maxConcurrent) {
        this.semaphore = maxConcurrent > 0 ? new Semaphore(maxConcurrent) : null;
    }

    public Token acquire() throws InterruptedException {
        if (semaphore != null) {
            semaphore.acquire();
            return new Token(semaphore);
        }
        return Token.NOOP;
    }

    public static final class Token {
        static final Token NOOP = new Token(null);
        private final Semaphore semaphore;

        Token(Semaphore semaphore) {
            this.semaphore = semaphore;
        }

        public void release() {
            if (semaphore != null) {
                semaphore.release();
            }
        }
    }
}
