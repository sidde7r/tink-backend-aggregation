package se.tink.backend.aggregation.utils;

/**
 * Thought usages: E.g. when doing BankId looping to avoid having Thread.sleep all over our code
 * base.
 */
public interface RateLimitedCountdown {
    /** @return boolean(counter greater than 0), execution is delayed based on rate limit. */
    boolean acquireIsMore();
}
