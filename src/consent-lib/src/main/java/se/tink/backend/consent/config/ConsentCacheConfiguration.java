package se.tink.backend.consent.config;

import java.util.concurrent.TimeUnit;

public class ConsentCacheConfiguration {
    private int duration = 20;
    private TimeUnit timeUnit = TimeUnit.MINUTES;

    public int getDuration() {
        return duration;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    /**
     * Returns a cache with a duration of 1ns. Duration needs to be > 0 to be used in Suppliers.
     */
    public static ConsentCacheConfiguration NoCache() {
        ConsentCacheConfiguration config = new ConsentCacheConfiguration();
        config.duration = 1;
        config.timeUnit = TimeUnit.NANOSECONDS;

        return config;
    }

    /**
     * Return a default cache with expiration on 20 minutes.
     */
    public static ConsentCacheConfiguration Default() {
        return new ConsentCacheConfiguration();
    }
}
