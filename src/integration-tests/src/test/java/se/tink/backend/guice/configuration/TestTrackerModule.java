package se.tink.backend.guice.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.config.AnalyticsConfiguration;
import se.tink.backend.common.config.DistributedDatabaseConfiguration;
import se.tink.backend.common.repository.cassandra.EventRepository;
import se.tink.backend.common.tracking.EventTracker;
import se.tink.backend.common.tracking.ForwardingTracker;
import se.tink.backend.common.tracking.PersistingTracker;
import se.tink.backend.common.tracking.TrackableEvent;
import se.tink.backend.common.tracking.intercom.IntercomTracker;

import javax.annotation.Nullable;

public class TestTrackerModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    public EventTracker provideEventTracker(DistributedDatabaseConfiguration distributedDatabaseConfiguration,
                                            AnalyticsConfiguration analyticsConfiguration,
                                            @Named("trackingExecutor") ListenableThreadPoolExecutor<Runnable> trackingExecutorService,
                                            @Nullable EventRepository eventRepository) {
        EventTracker eventTracker = new EventTracker() {
            @Override
            public void trackUserProperties(TrackableEvent event) {
                // Noop for tests
            }

            @Override
            public void trackEvent(TrackableEvent event) {
                // Noop for tests
            }
        };

        return new ForwardingTracker(trackingExecutorService,
                new PersistingTracker(eventRepository),
                eventTracker
        );
    }
}
