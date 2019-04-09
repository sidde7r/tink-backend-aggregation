package se.tink.libraries.repository.guice.configuration;

import com.google.inject.AbstractModule;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import se.tink.libraries.repository.config.DatabaseConfiguration;
import se.tink.libraries.repository.config.repository.PersistenceUnit;
import se.tink.libraries.repository.config.repository.SingletonRepositoryConfiguration;
import se.tink.libraries.repository.guice.annotations.Centralized;
import se.tink.libraries.repository.providers.SpringIntegrationProvider;

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
            bind(AnnotationConfigApplicationContext.class)
                    .annotatedWith(Centralized.class)
                    .toInstance(applicationContext);
        }

        bindCaches();

        bindCentralizedDaos();

        bindRepositories();
    }

    protected void bindCentralizedDaos() {}

    protected void bindRepositories() {}

    protected void bindCaches() {}

    private AnnotationConfigApplicationContext centralizedApplicationContext() {
        SingletonRepositoryConfiguration.setConfiguration(databaseConfiguration);
        PersistenceUnit persistenceUnit =
                PersistenceUnit.fromName(databaseConfiguration.getPersistenceUnitName());
        return new AnnotationConfigApplicationContext(persistenceUnit.getConfiguratorKlass());
    }

    protected <T> void bindSpringBean(Class<T> clazz) {
        bind(clazz).toProvider(SpringIntegrationProvider.fromSpring(clazz));
    }
}
