package se.tink.backend.common.tracking;


/**
 * Trackers should be thread-safe
 */
public interface EventTracker {

    void trackUserProperties(final TrackableEvent event);
    void trackEvent(final TrackableEvent event);

    class Properties {
        public static final String EMAIL = "$email";
        public static final String NAME = "$name";
        public static final String CREATED = "$created";
    }
}
