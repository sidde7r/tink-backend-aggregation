package se.tink.backend.main.controllers;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import se.tink.backend.common.repository.cassandra.TrackingEventRepository;
import se.tink.backend.common.repository.cassandra.TrackingSessionRepository;
import se.tink.backend.common.repository.cassandra.TrackingTimingRepository;
import se.tink.backend.common.repository.cassandra.TrackingViewRepository;
import se.tink.backend.common.utils.MetricsUtils;
import se.tink.backend.core.tracking.TrackingEvent;
import se.tink.backend.core.tracking.TrackingSession;
import se.tink.backend.core.tracking.TrackingTiming;
import se.tink.backend.core.tracking.TrackingView;
import se.tink.backend.rpc.TrackSessionCommand;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.uuid.UUIDUtils;

public class TrackingServiceController {
    private static final int BATCH_SIZE = 150;

    private TrackingSessionRepository trackingSessionRepository;
    private TrackingEventRepository trackingEventRepository;
    private TrackingTimingRepository trackingTimingRepository;
    private TrackingViewRepository trackingViewRepository;
    private MetricRegistry metricRegistry;

    @Inject
    public TrackingServiceController(
            TrackingSessionRepository trackingSessionRepository,
            TrackingEventRepository trackingEventRepository,
            TrackingTimingRepository trackingTimingRepository,
            TrackingViewRepository trackingViewRepository,
            MetricRegistry metricRegistry) {
        this.trackingSessionRepository = trackingSessionRepository;
        this.trackingEventRepository = trackingEventRepository;
        this.trackingTimingRepository = trackingTimingRepository;
        this.trackingViewRepository = trackingViewRepository;
        this.metricRegistry = metricRegistry;
    }

    public TrackingSession createSession() {
        return trackingSessionRepository.save(new TrackingSession());
    }

    public void track(TrackSessionCommand command) {
        TrackingSession trackingSession = trackingSessionRepository.findOne(UUIDUtils.fromTinkUUID(command.getSessionId()));

        if (trackingSession == null) {
            trackingSession = new TrackingSession();
            trackingSession.setId(UUIDUtils.fromTinkUUID(command.getSessionId()));
            trackingSessionRepository.save(trackingSession);
        }

        if (command.getUserId().isPresent() && trackingSession.getId() == null) {
            trackingSession.setUserId(UUIDUtils.fromTinkUUID(command.getUserId().get()));
            trackingSessionRepository.save(trackingSession);
        }

        final UUID trackingSessionId = trackingSession.getId();

        saveTrackingEvents(command.getTrackingEvents(), command.getClientClock(), trackingSessionId);
        saveTrackingTimings(command.getTrackingTimings(), command.getClientClock(), trackingSessionId);
        saveTrackingViews(command.getTrackingViews(), command.getClientClock(), trackingSessionId);
        registerMetrics(command.getTrackingTimings());
    }

    private void saveTrackingEvents(List<TrackingEvent> events, Date clock, UUID sessionId) {
        events.forEach( event -> {
            event.setId(UUIDs.timeBased());
            event.setSessionId(sessionId);
            event.setDate(DateUtils.offsetDateWithClientClock(clock, event.getDate()));
        });

        for (List<TrackingEvent> batch : Iterables.partition(events, BATCH_SIZE)) {
            trackingEventRepository.save(batch);
        }
    }

    private void saveTrackingTimings(List<TrackingTiming> timings, Date clock, UUID sessionId) {
        timings.forEach( timing -> {
            timing.setId(UUIDs.timeBased());
            timing.setSessionId(sessionId);
            timing.setDate(DateUtils.offsetDateWithClientClock(clock, timing.getDate()));
        });

        for (List<TrackingTiming> batch : Iterables.partition(timings, BATCH_SIZE)) {
            trackingTimingRepository.save(batch);
        }
    }

    private void saveTrackingViews(List<TrackingView> views, Date clock, UUID sessionId) {
        views.forEach( view -> {
            view.setId(UUIDs.timeBased());
            view.setSessionId(sessionId);
            view.setDate(DateUtils.offsetDateWithClientClock(clock, view.getDate()));
        });

        for (List<TrackingView> batch : Iterables.partition(views, BATCH_SIZE)) {
            trackingViewRepository.save(batch);
        }
    }

    private void registerMetrics(List<TrackingTiming> timings) {
        timings.forEach( timing -> {
            if (timing.getTime() != null) {
                MetricId metricName = MetricId.newId("tracking_service_duration")
                        .label("category", timing.getCategory())
                        .label("timing", MetricsUtils.cleanMetricName(timing.getName()))
                        .label("label", timing.getLabel());
                metricRegistry.histogram(metricName).update((double) timing.getTime() / 1000);
            }
        });
    }

}
