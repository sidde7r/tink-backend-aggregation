package se.tink.backend.common.controllers;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.common.tracking.EventTracker;
import se.tink.backend.common.tracking.PersistingTracker;
import se.tink.backend.common.tracking.TrackableEvent;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;

/***
 * Class, which contains business logic for analytics-related things.
 */
// TODO: create only one instance of this class
public class AnalyticsController {
    private final static LogUtils log = new LogUtils(AnalyticsController.class);
    private static final ImmutableSet<Class<? extends EventTracker>> INTERNAL_TRACKERS =
            ImmutableSet.<Class<? extends EventTracker>>builder().add(PersistingTracker.class).build();

    private final EventTracker eventTracker;

    @Inject
    public AnalyticsController(EventTracker eventTracker) {
        this.eventTracker = eventTracker;
    }

    public void trackUserProperties(final User user, final Map<String, Object> properties) {
        final TrackableEvent event = TrackableEvent.userProperties(user.getId(), properties);

        if (!Strings.isNullOrEmpty(user.getUsername())) {
            event.addProperty(EventTracker.Properties.EMAIL, user.getUsername());
        }

        if (user.getProfile() != null && nameShouldBeTracked(user.getProfile().getName())) {
            event.addProperty(EventTracker.Properties.NAME, user.getProfile().getName());
        }

        if (user.getCreated() != null) {
            event.addProperty(EventTracker.Properties.CREATED, user.getCreated());
        }

        eventTracker.trackUserProperties(event);
    }

    private static boolean nameShouldBeTracked(String name) {
        if (Strings.isNullOrEmpty(StringUtils.trimToNull(name))) {
            return false;
        }

        final String lowerCaseName = name.toLowerCase();

        if (Objects.equals(lowerCaseName, "person skyddad identitet")) {
            return false;
        }

        if (Objects.equals(lowerCaseName, "person emigrerad")) {
            return false;
        }

        if (Objects.equals(lowerCaseName, "person har ändrat personnummer")) {
            return false;
        }

        if (Objects.equals(lowerCaseName, "person avliden")) {
            return false;
        }

        return true;
    }

    /**
     * Track an event asynchronously.
     *
     * @param user       the user who submitted the event.
     * @param type       the type of event
     * @param properties event properties
     * @param includedTrackers   if specified, only send events to these trackers
     * @note That this method <i>must</i> return fast and be very stable. There are a lot of code paths that depend on
     * this.
     */
    private void trackAsync(User user, String type, Map<String, Object> properties,
            ImmutableSet<Class<? extends EventTracker>> includedTrackers) {

        if (user == null) {
            log.warn("User is null. Not tracking.");
            return;
        }

        if (!user.isTrackingEnabled()) {
            log.info(user.getId(), "Event tracking disabled. Event: " + type);
            return;
        }

        TrackableEvent event = TrackableEvent.event(user.getId(), type, properties);

        if (includedTrackers != null) {
            for (Class<? extends EventTracker> includedTracker : includedTrackers) {
                event.includeTracker(includedTracker);
            }
        }

        eventTracker.trackEvent(event);
    }

    public void trackEvent(User user, String type, Map<String, Object> properties,
            ImmutableSet<Class<? extends EventTracker>> includedTrackers) {
        trackAsync(user, type, properties, includedTrackers);
    }

    public void trackEvent(User user, String type, Map<String, Object> properties) {
        trackEvent(user, type, properties, null);
    }

    public void trackEvent(User user, String type, ImmutableSet<Class<? extends EventTracker>> includedTrackers) {
        trackEvent(user, type, null, includedTrackers);
    }

    public void trackEvent(User user, String type) {
        trackEvent(user, type, null, null);
    }

    public void trackEventInternally(User user, String type, Map<String, Object> properties) {
        trackAsync(user, type, properties, INTERNAL_TRACKERS);
    }

    public void trackEventInternally(User user, String type) {
        trackEventInternally(user, type, null);
    }

    /**
     * Events triggered by the user. The events will automatically be populated with the remote IP address.
     */
    public void trackUserEvent(User user, String type, Map<String, Object> properties, Optional<String> remoteIp,
            ImmutableSet<Class<? extends EventTracker>> includedTrackers) {
        if (remoteIp.isPresent()) {

            if (properties == null) {
                properties = Maps.newHashMap();
            }
        }

        trackAsync(user, type, properties, includedTrackers);
    }

    public void trackUserEvent(User user, String type, Optional<String> remoteIp) {
        trackUserEvent(user, type, remoteIp, null);
    }

    public void trackUserEvent(User user, String type, String remoteIp) {
        trackUserEvent(user, type, Optional.of(remoteIp), null);
    }

    public void trackUserEvent(User user, String type, Optional<String> remoteIp,
            ImmutableSet<Class<? extends EventTracker>> includedTrackers) {
        trackUserEvent(user, type, null, remoteIp, includedTrackers);
    }

    public void trackUserEvent(User user, String type, Map<String, Object> properties, Optional<String> remoteIp) {
        trackUserEvent(user, type, properties, remoteIp, null);
    }
}
