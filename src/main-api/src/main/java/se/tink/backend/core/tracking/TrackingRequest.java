package se.tink.backend.core.tracking;

import io.protostuff.Tag;

import java.util.List;

public class TrackingRequest {
    @Tag(1)
    private List<TrackingEvent> events;
    @Tag(2)
    private List<TrackingTiming> timings;
    @Tag(3)
    private List<TrackingView> views;

    public List<TrackingEvent> getEvents() {
        return events;
    }

    public List<TrackingTiming> getTimings() {
        return timings;
    }

    public List<TrackingView> getViews() {
        return views;
    }

    public void setEvents(List<TrackingEvent> events) {
        this.events = events;
    }

    public void setTimings(List<TrackingTiming> timings) {
        this.timings = timings;
    }

    public void setViews(List<TrackingView> views) {
        this.views = views;
    }
}
