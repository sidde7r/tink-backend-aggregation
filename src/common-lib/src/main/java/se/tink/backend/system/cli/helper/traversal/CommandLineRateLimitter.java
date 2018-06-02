package se.tink.backend.system.cli.helper.traversal;

import com.google.common.util.concurrent.RateLimiter;
import java.util.Optional;
import rx.functions.Func1;

public class CommandLineRateLimitter<T> implements Func1<T, T> {
    private static final String RATE_PER_SECOND = "ratePerSecond";

    private static final Double ratePerSecond = Optional.ofNullable(System.getProperty(RATE_PER_SECOND))
            .map(Double::valueOf)
            .orElse(Double.MAX_VALUE);

    private final RateLimiter rateLimitter = RateLimiter.create(ratePerSecond);

    @Override
    public T call(T t) {
        rateLimitter.acquire();
        return t;
    }

}
