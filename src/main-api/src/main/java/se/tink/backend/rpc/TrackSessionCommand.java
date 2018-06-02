package se.tink.backend.rpc;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import se.tink.backend.core.tracking.TrackingEvent;
import se.tink.backend.core.tracking.TrackingTiming;
import se.tink.backend.core.tracking.TrackingView;

public class TrackSessionCommand {
    private String sessionId;
    private Optional<String> userId;
    private List<TrackingEvent> trackingEvents;
    private List<TrackingTiming> trackingTimings;
    private List<TrackingView> trackingViews;
    private Date clientClock;

    public TrackSessionCommand(String sessionId, Optional<String> userId,
            List<TrackingEvent> trackingEvents,
            List<TrackingTiming> trackingTimings, List<TrackingView> trackingViews, Date clientClock) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.trackingEvents = trackingEvents;
        this.trackingTimings = trackingTimings;
        this.trackingViews = trackingViews;
        this.clientClock = clientClock;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Optional<String> getUserId() {
        return userId;
    }

    public List<TrackingEvent> getTrackingEvents() {
        return trackingEvents;
    }

    public List<TrackingTiming> getTrackingTimings() {
        return trackingTimings;
    }

    public List<TrackingView> getTrackingViews() {
        return trackingViews;
    }

    public Date getClientClock() {
        return clientClock;
    }
}
