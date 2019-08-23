package se.tink.backend.aggregation.nxgen.controllers.authentication;

import java.util.concurrent.TimeUnit;

public final class SupplementalWaitRequest {

    private final String key;
    private final long waitFor;
    private final TimeUnit timeUnit;

    public SupplementalWaitRequest(final String key, final long waitFor, final TimeUnit timeUnit) {
        this.key = key;
        this.waitFor = waitFor;
        this.timeUnit = timeUnit;
    }

    public String getKey() {
        return key;
    }

    public long getWaitFor() {
        return waitFor;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}
