package se.tink.libraries.retrypolicy;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class RetryPolicy {

    private final int maxAttempts;

    private final Set<Class> retryables;

    RetryPolicy(final int maxAttempts, final Class... t) {
        this.maxAttempts = maxAttempts;
        this.retryables = Collections.unmodifiableSet(Arrays.stream(t).collect(Collectors.toSet()));
    }

    boolean canRetry(final RetryContext context) {
        Throwable t = context.getLastThrowable();
        return (t == null || isOneOfRetryables(t)) && context.getAttempt() < this.maxAttempts;
    }

    public boolean isOneOfRetryables(Throwable t) {
        Set<Class> throwableSuperclasses = getThrowableSuperclasses(t);
        throwableSuperclasses.retainAll(retryables);
        return !throwableSuperclasses.isEmpty();
    }

    private Set<Class> getThrowableSuperclasses(Throwable t) {
        Set<Class> throwableSuperclasses = new HashSet<>();
        Class c = t.getClass();
        while (c != Object.class) {
            throwableSuperclasses.add(c);
            c = c.getSuperclass();
        }
        return throwableSuperclasses;
    }
}
