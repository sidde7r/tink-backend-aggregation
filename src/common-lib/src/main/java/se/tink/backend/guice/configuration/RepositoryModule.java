package se.tink.backend.guice.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.util.Providers;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.common.config.DistributedDatabaseConfiguration;
import se.tink.backend.common.config.DistributedRepositoryConfiguration;
import se.tink.backend.common.config.repository.PersistenceUnit;
import se.tink.backend.common.config.repository.SingletonRepositoryConfiguration;
import se.tink.backend.common.providers.SpringIntegrationProvider;
import se.tink.backend.guice.annotations.Centralized;
import se.tink.backend.guice.annotations.Distributed;

public abstract class RepositoryModule extends AbstractModule {
    private final DatabaseConfiguration databaseConfiguration;
    private final DistributedDatabaseConfiguration distributedDatabaseConfiguration;

    public RepositoryModule(DatabaseConfiguration databaseConfiguration,
            DistributedDatabaseConfiguration distributedDatabaseConfiguration) {
        this.databaseConfiguration = databaseConfiguration;
        this.distributedDatabaseConfiguration = distributedDatabaseConfiguration;
    }

    @Override
    protected void configure() {
        // Initialize Spring applicationContext
        if (databaseConfiguration.isEnabled()) {
            AnnotationConfigApplicationContext applicationContext = centralizedApplicationContext();
            bind(AnnotationConfigApplicationContext.class).annotatedWith(Centralized.class)
                    .toInstance(applicationContext);
        }

        if (distributedDatabaseConfiguration != null && distributedDatabaseConfiguration.isEnabled()) {
            AnnotationConfigApplicationContext distributedApplicationContext = distributedApplicationContext();
            bind(AnnotationConfigApplicationContext.class).annotatedWith(Distributed.class)
                    .toInstance(distributedApplicationContext);
        } else {
            bind(AnnotationConfigApplicationContext.class).annotatedWith(Distributed.class).toProvider(
                    Providers.<AnnotationConfigApplicationContext>of(null));
        }

        bindCaches();

        bindCentralizedDaos();

        // DAOs that require distributed database
        if (distributedDatabaseConfiguration != null && distributedDatabaseConfiguration.isEnabled()) {
            bindDistributedDaos();
        }

        bindRepositories();
    }

    protected void bindCentralizedDaos() {
    }

    protected void bindDistributedDaos() {
    }

    protected void bindRepositories() {
    }

    protected void bindCaches() {
    }

    private AnnotationConfigApplicationContext centralizedApplicationContext() {
        SingletonRepositoryConfiguration.setConfiguration(databaseConfiguration);
        PersistenceUnit persistenceUnit = PersistenceUnit.fromName(databaseConfiguration.getPersistenceUnitName());
        return new AnnotationConfigApplicationContext(persistenceUnit.getConfiguratorKlass());
    }

    private AnnotationConfigApplicationContext distributedApplicationContext() {
        DistributedRepositoryConfiguration.setConfiguration(distributedDatabaseConfiguration);
        return new AnnotationConfigApplicationContext(DistributedRepositoryConfiguration.class);
    }

    protected <T> void bindSpringBean(Class<T> clazz) {
        boolean isCassandra = isCassandraRepository(clazz);
        if (isCassandra && distributedDatabaseConfiguration == null || isCassandra && !distributedDatabaseConfiguration
                .isEnabled() || !isCassandra && !databaseConfiguration.isEnabled()) {
            return;
        }

        bind(clazz).toProvider(SpringIntegrationProvider.fromSpring(clazz));
    }

    private <T> boolean isCassandraRepository(Class<T> clazz) {
        return CassandraRepository.class.isAssignableFrom(clazz);
    }
}
