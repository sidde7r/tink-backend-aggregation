package se.tink.backend.guice.configuration;

import com.google.inject.AbstractModule;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.common.config.repository.PersistenceUnit;
import se.tink.backend.common.config.repository.SingletonRepositoryConfiguration;
import se.tink.backend.common.providers.SpringIntegrationProvider;
import se.tink.backend.guice.annotations.Centralized;

public abstract class RepositoryModule extends AbstractModule {
    private final DatabaseConfiguration databaseConfiguration;

    public RepositoryModule(DatabaseConfiguration databaseConfiguration) {
        this.databaseConfiguration = databaseConfiguration;
    }

    @Override
    protected void configure() {
        // Initialize Spring applicationContext
        if (databaseConfiguration.isEnabled()) {
            AnnotationConfigApplicationContext applicationContext = centralizedApplicationContext();
            bind(AnnotationConfigApplicationContext.class).annotatedWith(Centralized.class)
                    .toInstance(applicationContext);
        }

        bindCaches();

        bindCentralizedDaos();

        bindRepositories();
    }

    protected void bindCentralizedDaos() {
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

    protected <T> void bindSpringBean(Class<T> clazz) {
        bind(clazz).toProvider(SpringIntegrationProvider.fromSpring(clazz));
    }
}
