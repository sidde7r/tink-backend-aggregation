package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.configuration.models.AggregationServiceConfiguration;
import se.tink.backend.aggregation.startupchecks.EidasProxySignerHealthCheck;
import se.tink.backend.aggregation.startupchecks.SecretsServiceHealthCheck;
import se.tink.backend.aggregation.startupchecks.StartupChecksHandler;
import se.tink.backend.aggregation.startupchecks.StartupChecksHandlerImpl;

public class AggregationHealthChecksModule extends AbstractModule {
    private final AggregationServiceConfiguration configuration;

    public AggregationHealthChecksModule(AggregationServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void configure() {
        bind(StartupChecksHandler.class).to(StartupChecksHandlerImpl.class).in(Scopes.SINGLETON);
        bind(SecretsServiceHealthCheck.class).in(Scopes.SINGLETON);

        bind(EidasProxySignerHealthCheck.class).in(Scopes.SINGLETON);
    }
}
