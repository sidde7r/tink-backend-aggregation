package se.tink.backend.aggregation.legacy;

import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.log.legacy.LogUtils;

/**
 * Do not use this class in new code anymore. This class is going to be removed.
 * <p>
 * Instead of it, create constructor with needed objects and use dependency injection to create an instance
 */
@Deprecated
public class ServiceContext implements Managed {
    private static final LogUtils log = new LogUtils(ServiceContext.class);

    private AnnotationConfigApplicationContext applicationContext;
    private CacheClient cacheClient;

    @Inject
    public ServiceContext(CacheClient cacheClient) {
        this.cacheClient = cacheClient;
    }

    private enum ManagedState {
        STARTING_OR_STARTED,
        STOPPING,
        STOPPED,
    }

    private AtomicReference<ManagedState> managedState = new AtomicReference<>(ManagedState.STOPPED);

    /**
     * Start the {@link ServiceContext}. Must support being called multiple times.
     */
    @PostConstruct
    @Override
    public void start() throws Exception {
        if (!managedState.compareAndSet(ManagedState.STOPPED, ManagedState.STARTING_OR_STARTED)) {
            // Avoid being initialized concurrently or multiple times.
            log.info("Already started. Not starting again.");
            return;
        }
    }

    /**
     * Stop.
     * <p>
     * Should be callable multiple times consecutively.
     */
    @PreDestroy
    @Override
    public void stop() throws Exception {
        if (!managedState.compareAndSet(ManagedState.STARTING_OR_STARTED, ManagedState.STOPPING)) {
            // Avoid stopping concurrently or when start has not been called.
            log.info("Start not called or stopped already called. Not stopping.");
            return;
        }

        try {
            log.info("Stopping...");

            if (cacheClient != null) {
                cacheClient.shutdown();
                cacheClient = null;
            }

            if (applicationContext != null) {
                applicationContext.close();
                applicationContext = null;
            }

            log.info("Stopped.");
        } finally {
            managedState.set(ManagedState.STOPPED);
        }

    }

}
