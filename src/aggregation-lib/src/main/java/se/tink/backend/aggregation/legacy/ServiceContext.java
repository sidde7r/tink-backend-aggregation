package se.tink.backend.aggregation.legacy;

import com.google.common.base.Preconditions;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.dropwizard.lifecycle.Managed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import se.tink.backend.aggregation.configurations.AggregationServiceConfiguration;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.common.config.repository.PersistenceUnit;
import se.tink.backend.common.config.repository.SingletonRepositoryConfiguration;
import se.tink.backend.common.repository.RepositoryFactory;
import se.tink.backend.common.utils.ExecutorServiceUtils;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.draining.ApplicationDrainMode;
import se.tink.libraries.repository.RepositorySource;

/**
 * Do not use this class in new code anymore. This class is going to be removed.
 * <p>
 * Instead of it, create constructor with needed objects and use dependency injection to create an instance
 */
@Deprecated
public class ServiceContext implements Managed, RepositoryFactory {
    private static final LogUtils log = new LogUtils(ServiceContext.class);

    private AnnotationConfigApplicationContext applicationContext;
    private CacheClient cacheClient;
    private final AggregationServiceConfiguration configuration;
    private CuratorFramework zookeeperClient;
    private LoadingCache<Class<?>, Object> DAOs;
    private ApplicationDrainMode applicationDrainMode;

    private ListenableThreadPoolExecutor<Runnable> trackingExecutorService;

    /**
     * Thread pool used for queuing various general asynchronous tasks. Mostly used to return faster to HTTP clients.
     */

    private ListenableThreadPoolExecutor<Runnable> executorService;

    @Inject
    public ServiceContext(final AggregationServiceConfiguration configuration,
            CacheClient cacheClient,
            CuratorFramework zookeeperClient,
            @Named("executor") ListenableThreadPoolExecutor<Runnable> executorService,
            @Named("trackingExecutor") ListenableThreadPoolExecutor<Runnable> trackingExecutorService,
            ApplicationDrainMode applicationDrainMode) {
        this.cacheClient = cacheClient;
        this.zookeeperClient = zookeeperClient;
        this.configuration = configuration;
        this.executorService = executorService;
        this.trackingExecutorService = trackingExecutorService;
        this.applicationDrainMode = applicationDrainMode;
    }

    public CacheClient getCacheClient() {
        return cacheClient;
    }

    public AggregationServiceConfiguration getConfiguration() {
        return configuration;
    }

    public CuratorFramework getCoordinationClient() {
        return zookeeperClient;
    }

    private enum ManagedState {
        STARTING_OR_STARTED,
        STOPPING,
        STOPPED,
    }

    private AtomicReference<ManagedState> managedState = new AtomicReference<>(ManagedState.STOPPED);

    @Override
    public <R> R getRepository(Class<R> cls) {
        return getRepository(cls, RepositorySource.DEFAULT);
    }

    @SuppressWarnings("unchecked")
    private <R> R getRepository(Class<R> cls, RepositorySource source) {
        Preconditions.checkNotNull(source);

        switch (source) {
        case CENTRALIZED:
            Preconditions.checkState(applicationContext != null,
                    "ServiceContext is not initialized/managed. Call start().");
            return applicationContext.getBean(cls);
        default:
            // Default to centralized
            return getRepository(cls, RepositorySource.CENTRALIZED);
        }
    }

    public ListenableThreadPoolExecutor<Runnable> getExecutorService() {
        return executorService;
    }

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

    public ListenableThreadPoolExecutor<Runnable> getTrackingExecutorService() {
        return trackingExecutorService;
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

    public <T> T getDao(Class<T> key) {
        return key.cast(DAOs.getUnchecked(key));
    }

    public ApplicationDrainMode getApplicationDrainMode() {
        return applicationDrainMode;
    }
}
