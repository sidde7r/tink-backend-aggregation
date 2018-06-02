package se.tink.backend.guice.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import javax.annotation.Nullable;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.config.AnalyticsConfiguration;
import se.tink.backend.common.config.DistributedDatabaseConfiguration;
import se.tink.backend.common.repository.cassandra.EventRepository;
import se.tink.backend.common.tracking.EventTracker;
import se.tink.backend.common.tracking.ForwardingTracker;
import se.tink.backend.common.tracking.PersistingTracker;
import se.tink.backend.common.tracking.intercom.IntercomTracker;

public class EventTrackerModule extends AbstractModule {
    @Override
    protected void configure() {
        // Deliberately left empty because of the @Provides method below.
    }

    @Provides
    @Singleton
    public EventTracker provideEventTracker(DistributedDatabaseConfiguration distributedDatabaseConfiguration,
            AnalyticsConfiguration analyticsConfiguration,
            @Named("trackingExecutor") ListenableThreadPoolExecutor<Runnable> trackingExecutorService,
            @Nullable EventRepository eventRepository) {
        if (distributedDatabaseConfiguration != null && distributedDatabaseConfiguration.isEnabled()
                && eventRepository != null) {
            return new ForwardingTracker(
                    trackingExecutorService,
                    new PersistingTracker(eventRepository),
                    new IntercomTracker(analyticsConfiguration.getIntercom()));

        } else {
            return new ForwardingTracker(
                    trackingExecutorService,
                    new IntercomTracker(analyticsConfiguration.getIntercom()));
        }
    }
}
