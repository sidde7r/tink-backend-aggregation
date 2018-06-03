package se.tink.backend.common;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
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
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.client.ServiceFactory;
import se.tink.backend.common.admin.ApplicationDrainMode;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.config.repository.PersistenceUnit;
import se.tink.backend.common.config.repository.SingletonRepositoryConfiguration;
import se.tink.backend.common.dao.ActivityDao;
import se.tink.backend.common.dao.ApplicationDAO;
import se.tink.backend.common.dao.DeviceConfigurationDao;
import se.tink.backend.common.dao.InvestmentDao;
import se.tink.backend.common.dao.NotificationDao;
import se.tink.backend.common.dao.ProductDAO;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.dao.StatisticDao;
import se.tink.backend.common.dao.transactions.TransactionRepository;
import se.tink.backend.common.repository.RepositoryFactory;
import se.tink.backend.common.repository.cassandra.CassandraStatisticRepository;
import se.tink.backend.common.repository.cassandra.DAO.LoanDAO;
import se.tink.backend.common.repository.cassandra.InstrumentHistoryRepository;
import se.tink.backend.common.repository.cassandra.InstrumentRepository;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.cassandra.LoanDetailsRepository;
import se.tink.backend.common.repository.cassandra.NotificationEventRepository;
import se.tink.backend.common.repository.cassandra.PortfolioHistoryRepository;
import se.tink.backend.common.repository.cassandra.PortfolioRepository;
import se.tink.backend.common.repository.cassandra.ProductFilterRepository;
import se.tink.backend.common.repository.cassandra.ProductInstanceRepository;
import se.tink.backend.common.repository.cassandra.ProductTemplateRepository;
import se.tink.backend.common.repository.mysql.main.ActivityRepository;
import se.tink.backend.common.repository.mysql.main.ApplicationFormRepository;
import se.tink.backend.common.repository.mysql.main.ApplicationRepository;
import se.tink.backend.common.repository.mysql.main.DeviceConfigurationRepository;
import se.tink.backend.common.repository.mysql.main.NotificationRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.tracking.EventTracker;
import se.tink.backend.common.utils.ExecutorServiceUtils;
import se.tink.backend.encryption.client.EncryptionServiceFactory;
import se.tink.backend.guice.configuration.ProviderCacheConfiguration;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.MeterFactory;
import se.tink.libraries.metrics.MetricRegistry;

/**
 * Do not use this class in new code anymore. This class is going to be removed.
 * <p>
 * Instead of it, create constructor with needed objects and use dependency injection to create an instance
 */
@Deprecated
public class ServiceContext implements Managed, RepositoryFactory {
    private static final LogUtils log = new LogUtils(ServiceContext.class);

    private final boolean isUseAggregationController;
    private final AggregationControllerCommonClient aggregationControllerCommonClient;
    private final AggregationServiceFactory aggregationServiceFactory;
    private AnnotationConfigApplicationContext applicationContext;
    private CacheClient cacheClient;
    private final ServiceConfiguration configuration;
    private CuratorFramework zookeeperClient;
    private final MetricRegistry metricRegistry;
    private final ServiceFactory serviceFactory;
    private final SystemServiceFactory systemServiceFactory;
    private LoadingCache<Class<?>, Object> DAOs;
    private final EventTracker eventTracker;
    private ApplicationDrainMode applicationDrainMode;
    private final boolean supplementalOnAggregation;
    private final boolean isAggregationCluster;
    private final boolean isProvidersOnAggregation;

    private ListenableThreadPoolExecutor<Runnable> trackingExecutorService;

    /**
     * Thread pool used for queuing various general asynchronous tasks. Mostly used to return faster to HTTP clients.
     */

    private ListenableThreadPoolExecutor<Runnable> executorService;
    private AnnotationConfigApplicationContext distributedApplicationContext;

    @Inject
    public ServiceContext(@Named("useAggregationController") boolean isUseAggregationController,
            AggregationControllerCommonClient aggregationControllerCommonClient,
            final ServiceConfiguration configuration, MetricRegistry metricRegistry,
            CacheClient cacheClient, CuratorFramework zookeeperClient,
            ServiceFactory serviceFactory, SystemServiceFactory systemServiceFactory,
            AggregationServiceFactory aggregationServiceFactory,
            EventTracker eventTracker,
            EncryptionServiceFactory encryptionServiceFactory,
            ApplicationDrainMode applicationDrainMode,
            @Named("executor") ListenableThreadPoolExecutor<Runnable> executorService,
            @Named("trackingExecutor") ListenableThreadPoolExecutor<Runnable> trackingExecutorService,
            @Named("isSupplementalOnAggregation") boolean supplementalOnAggregation,
            @Named("isAggregationCluster") boolean isAggregationCluster,
            @Named("isProvidersOnAggregation") boolean isProvidersOnAggregation) {

        this.isUseAggregationController = isUseAggregationController;
        this.aggregationControllerCommonClient = aggregationControllerCommonClient;
        this.serviceFactory = serviceFactory;
        this.systemServiceFactory = systemServiceFactory;
        this.aggregationServiceFactory = aggregationServiceFactory;
        this.cacheClient = cacheClient;
        this.zookeeperClient = zookeeperClient;
        this.configuration = configuration;
        this.metricRegistry = metricRegistry;
        this.eventTracker = eventTracker;
        this.applicationDrainMode = applicationDrainMode;
        this.encryptionServiceFactory = encryptionServiceFactory;
        this.executorService = executorService;
        this.trackingExecutorService = trackingExecutorService;
        this.supplementalOnAggregation = supplementalOnAggregation;
        this.isAggregationCluster = isAggregationCluster;
        this.isProvidersOnAggregation = isProvidersOnAggregation;
    }

    public AggregationServiceFactory getAggregationServiceFactory() {
        return aggregationServiceFactory;
    }

    public CacheClient getCacheClient() {
        return cacheClient;
    }

    public ServiceConfiguration getConfiguration() {
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

    private TransactionRepository transactionsByUserIdAndPeriodRepository;

    private final EncryptionServiceFactory encryptionServiceFactory;

    public enum RepositorySource {
        /**
         * See #getRepository implementation.
         */
        DEFAULT,

        /**
         * Cassandra.
         */
        DISTRIBUTED,

        /**
         * MySQL.
         */
        CENTRALIZED,
    }

    @Override
    public <R> R getRepository(Class<R> cls) {
        return getRepository(cls, RepositorySource.DEFAULT);
    }

    @SuppressWarnings("unchecked")
    private <R> R getRepository(Class<R> cls, RepositorySource source) {
        Preconditions.checkNotNull(source);

        switch (source) {
        case DISTRIBUTED:
            Preconditions.checkState(distributedApplicationContext != null, "Distributed repository not initialized.");
            return distributedApplicationContext.getBean(cls);
        case CENTRALIZED:
            Preconditions.checkState(applicationContext != null,
                    "ServiceContext is not initialized/managed. Call start().");
            return applicationContext.getBean(cls);
        default:
            if (CassandraRepository.class.isAssignableFrom(cls)) {
                // There _are_ super interfaces of CassandraRepository, but this should cover 99% of our cases.
                return getRepository(cls, RepositorySource.DISTRIBUTED);
            }
            if (cls.equals(TransactionRepository.class)) {
                return (R) transactionsByUserIdAndPeriodRepository;
            }
            // Default to centralized
            return getRepository(cls, RepositorySource.CENTRALIZED);
        }
    }

    public ServiceFactory getServiceFactory() {
        return serviceFactory;
    }

    public SystemServiceFactory getSystemServiceFactory() {
        return systemServiceFactory;
    }

    public ListenableThreadPoolExecutor<Runnable> getExecutorService() {
        return executorService;
    }

    public void execute(final Runnable runnable) {
        executorService.execute(runnable);
    }

    /**
     * Start the {@link ServiceContext}. Must support being called multiple times. See comment in
     * {@link AbstractServiceContainer} as to why.
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

            initializeDAOs();
        }

        log.info("Started.");
    }

    private void initializeDAOs() {
        DAOs = CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, Object>() {

            @Override
            public Object load(Class<?> key) throws Exception {
                if (key.equals(StatisticDao.class)) {
                    return new StatisticDao(
                            getRepository(CassandraStatisticRepository.class), cacheClient, metricRegistry);
                } else if (key.equals(ActivityDao.class)) {
                    return new ActivityDao(
                            getRepository(ActivityRepository.class),
                            cacheClient,
                            new MeterFactory(metricRegistry));
                } else if (key.equals(ApplicationDAO.class)) {
                    return new ApplicationDAO(
                            getRepository(ApplicationRepository.class),
                            getRepository(ApplicationFormRepository.class)
                    );
                } else if (key.equals(NotificationDao.class)) {
                    return new NotificationDao(getRepository(NotificationRepository.class),
                            getRepository(NotificationEventRepository.class),
                            new MeterFactory(metricRegistry));
                } else if (key.equals(LoanDAO.class)) {
                    return new LoanDAO(
                            getRepository(LoanDataRepository.class),
                            getRepository(LoanDetailsRepository.class));
                } else if (key.equals(ProductDAO.class)) {
                    return new ProductDAO(
                            getRepository(ProductFilterRepository.class),
                            getRepository(ProductInstanceRepository.class),
                            getRepository(ProductTemplateRepository.class));
                } else if (key.equals(ProviderDao.class)) {
                    return new ProviderDao(
                            isProvidersOnAggregation,
                            getRepository(ProviderRepository.class),
                            aggregationControllerCommonClient,
                            new ProviderCacheConfiguration(5, TimeUnit.MINUTES));
                } else if (key.equals(DeviceConfigurationDao.class)) {
                    return new DeviceConfigurationDao(
                            getRepository(DeviceConfigurationRepository.class));
                } else if (key.equals(InvestmentDao.class)) {
                    return new InvestmentDao(
                            getRepository(PortfolioRepository.class),
                            getRepository(PortfolioHistoryRepository.class),
                            getRepository(InstrumentRepository.class),
                            getRepository(InstrumentHistoryRepository.class),
                            metricRegistry);
                } else {
                    throw new IllegalArgumentException("Class could not be instantiated");
                }
            }

        });
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
            if (distributedApplicationContext != null) {
                distributedApplicationContext.close();
                distributedApplicationContext = null;
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

    /**
     * Handler used to execute low-level Cassandra operations with.
     */
    public CassandraOperations getCassandraOperations() {
        return distributedApplicationContext.getBean("cassandraTemplate", CassandraOperations.class);
    }

    public EventTracker getEventTracker() {
        return eventTracker;
    }

    public EncryptionServiceFactory getEncryptionServiceFactory() {
        return encryptionServiceFactory;
    }

    public ApplicationDrainMode getApplicationDrainMode() {
        return applicationDrainMode;
    }

    public boolean isSupplementalOnAggregation() {
        return supplementalOnAggregation;
    }

    public boolean isUseAggregationController() {
        return isUseAggregationController;
    }

    public AggregationControllerCommonClient getAggregationControllerCommonClient() {
        return aggregationControllerCommonClient;
    }

    public boolean isAggregationCluster() {
        return isAggregationCluster;
    }

    public boolean isProvidersOnAggregation() {
        return isProvidersOnAggregation;
    }
}
