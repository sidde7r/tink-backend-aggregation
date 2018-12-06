package se.tink.backend.aggregation.legacy;

import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.dropwizard.lifecycle.Managed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import se.tink.backend.aggregation.configuration.models.AggregationServiceConfiguration;
import se.tink.backend.common.cache.CacheClient;
import se.tink.libraries.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.common.config.repository.PersistenceUnit;
import se.tink.backend.common.config.repository.SingletonRepositoryConfiguration;
import se.tink.backend.common.utils.ExecutorServiceUtils;
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
    private final AggregationServiceConfiguration configuration;
    private LoadingCache<Class<?>, Object> DAOs;

    private ListenableThreadPoolExecutor<Runnable> trackingExecutorService;

    /**
     * Thread pool used for queuing various general asynchronous tasks. Mostly used to return faster to HTTP clients.
     */

    private ListenableThreadPoolExecutor<Runnable> executorService;

    @Inject
    public ServiceContext(final AggregationServiceConfiguration configuration,
            CacheClient cacheClient,
            @Named("executor") ListenableThreadPoolExecutor<Runnable> executorService,
            @Named("trackingExecutor") ListenableThreadPoolExecutor<Runnable> trackingExecutorService) {
        this.cacheClient = cacheClient;
        this.configuration = configuration;
        this.executorService = executorService;
        this.trackingExecutorService = trackingExecutorService;
    }

    private enum ManagedState {
        STARTING_OR_STARTED,
        STOPPING,
        STOPPED,
    }

    private AtomicReference<ManagedState> managedState = new AtomicReference<>(ManagedState.STOPPED);

    public void execute(final Runnable runnable) {
        executorService.execute(runnable);
    }

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

        log.info("Starting...");

        final DatabaseConfiguration databaseConfiguration = configuration.getDatabase();
        PersistenceUnit persistenceUnit = null;
        if (databaseConfiguration.isEnabled()) {
            SingletonRepositoryConfiguration.setConfiguration(databaseConfiguration);
            persistenceUnit = PersistenceUnit.fromName(
                    databaseConfiguration.getPersistenceUnitName());
            applicationContext = new AnnotationConfigApplicationContext(persistenceUnit.getConfiguratorKlass());
        }

        log.info("Started.");
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

            if (executorService != null) {
                ExecutorServiceUtils.shutdownExecutor("ServiceContext#executorService", executorService, 2,
                        TimeUnit.MINUTES);
                executorService = null;
            }

            // Needs to be shut down after executorService since executorService is more likely to submit to
            // trackingService than the other way around.
            if (trackingExecutorService != null) {
                ExecutorServiceUtils.shutdownExecutor("ServiceContext#trackingService", trackingExecutorService, 20,
                        TimeUnit.SECONDS);
                trackingExecutorService = null;
            }

            if (cacheClient != null) {
                cacheClient.shutdown();
                cacheClient = null;
            }

            if (applicationContext != null) {
                applicationContext.close();
                applicationContext = null;
            }

            if (DAOs != null) {
                DAOs = null;
            }

            log.info("Stopped.");
        } finally {
            managedState.set(ManagedState.STOPPED);
        }

    }

}
