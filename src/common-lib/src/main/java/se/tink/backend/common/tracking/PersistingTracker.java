package se.tink.backend.common.tracking;

import se.tink.backend.common.repository.cassandra.EventRepository;
import se.tink.backend.core.Event;

public class PersistingTracker implements EventTracker {

    private EventRepository eventRepository;
    public PersistingTracker(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public void trackUserProperties(TrackableEvent event) {
        // We don't store UserPropery
    }

    @Override
    public void trackEvent(TrackableEvent event) {
        Event e = event.toStorableEvent();
        if (e != null) {
            eventRepository.save(e);
        }
    }
}
