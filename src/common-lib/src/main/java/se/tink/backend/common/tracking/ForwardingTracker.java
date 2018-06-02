package se.tink.backend.common.tracking;

import se.tink.backend.common.concurrency.ListenableExecutor;
import se.tink.backend.utils.LogUtils;

/**
 * Currently only usable from System container calls.
 * It should be fixed so that MainServiceResource tracks can be removed and use this instead.
 */
public class ForwardingTracker implements EventTracker {

    private static final LogUtils log = new LogUtils(ForwardingTracker.class);

    private final ListenableExecutor<Runnable> trackingExecutorService;
    private final EventTracker[] trackers;

    public ForwardingTracker(ListenableExecutor<Runnable> trackingExecutorService,
            EventTracker... trackers) {
        this.trackingExecutorService = trackingExecutorService;
        this.trackers = trackers;
    }

    @Override
    public void trackUserProperties(final TrackableEvent event) {
        log.debug(event.getUserId(), "Tracking user properties.");

        for (final EventTracker tracker : trackers) {

            if (!event.isTrackerIncluded(tracker.getClass())) {
                continue;
            }

            trackingExecutorService.execute(() -> tracker.trackUserProperties(event));
        }
    }

    @Override
    public void trackEvent(final TrackableEvent event) {
        log.debug(event.getUserId(), String.format("Tracking event: %s.", event.getEventType()));

        for (final EventTracker tracker : trackers) {

            if (!event.isTrackerIncluded(tracker.getClass())) {
                continue;
            }

            trackingExecutorService.execute(() -> tracker.trackEvent(event));
        }
    }
}
