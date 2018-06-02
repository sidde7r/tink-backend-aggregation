package se.tink.backend.connector.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import javax.inject.Singleton;
import se.tink.backend.categorization.client.CategorizationServiceFactory;
import se.tink.backend.common.client.CategorizationServiceFactoryProvider;
import se.tink.backend.common.client.SystemServiceFactoryProvider;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.tasks.interfaces.TaskSubmitter;
import se.tink.backend.common.tasks.kafka.KafkaTaskSubmitter;
import se.tink.backend.connector.auth.ConnectorAuthorizationFilterPredicates;
import se.tink.backend.system.client.SystemServiceFactory;

public class CommonConnectorModule extends AbstractModule {

    private ServiceConfiguration configuration;

    public CommonConnectorModule(ServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(SystemServiceFactory.class).toProvider(SystemServiceFactoryProvider.class).in(Scopes.SINGLETON);
        bind(CategorizationServiceFactory.class).toProvider(CategorizationServiceFactoryProvider.class).in(Scopes.SINGLETON);

        bind(TaskSubmitter.class).to(KafkaTaskSubmitter.class).in(Scopes.SINGLETON);
        bind(ConnectorAuthorizationFilterPredicates.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Named("clientTokens")
    @Singleton
    Map<String, List<String>> clientTokens() {
        return configuration.getConnector().getClients();
    }
}
