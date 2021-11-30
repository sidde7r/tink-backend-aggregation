package se.tink.agent.sdk.authentication.new_consent;

import java.time.Duration;

public class ConsentLifetime {
    private static final Duration SHORT_LIVED_LIFETIME = Duration.ofMinutes(15);

    private final Duration lifetime;

    private ConsentLifetime(Duration lifetime) {
        this.lifetime = lifetime;
    }

    public Duration getLifetime() {
        return lifetime;
    }

    // The same lifetime as we were tasked to create, e.g. 90 days for PSD2.
    public static ConsentLifetime defaultLifetime() {
        return new ConsentLifetime(Duration.ZERO);
    }

    // A "one-time" consent that is only valid for a short time.
    public static ConsentLifetime shortLifetime() {
        return new ConsentLifetime(SHORT_LIVED_LIFETIME);
    }

    public static ConsentLifetime infinite() {
        return null;
    }

    public static ConsentLifetime specificLifetime(Duration lifetime) {
        return new ConsentLifetime(lifetime);
    }
}
