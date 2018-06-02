package se.tink.backend.guice.configuration;

import java.util.concurrent.TimeUnit;

public class ProviderCacheConfiguration {
    private final int duration;
    private final TimeUnit timeUnit;

    public ProviderCacheConfiguration(int duration, TimeUnit timeUnit) {
        this.duration = duration;
        this.timeUnit = timeUnit;
    }

    public int getDuration() {
        return duration;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}
