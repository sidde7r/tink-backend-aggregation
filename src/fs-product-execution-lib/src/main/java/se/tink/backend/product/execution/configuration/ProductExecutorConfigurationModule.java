package se.tink.backend.product.execution.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.util.Providers;
import se.tink.backend.guice.annotations.SystemConfiguration;
import se.tink.libraries.discovery.CoordinationConfiguration;
import se.tink.libraries.endpoints.EndpointConfiguration;

public class ProductExecutorConfigurationModule extends AbstractModule {
    private final ProductExecutorConfiguration configuration;

    public ProductExecutorConfigurationModule(ProductExecutorConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(ProductExecutorConfiguration.class).toInstance(configuration);
        bind(EndpointConfiguration.class).annotatedWith(SystemConfiguration.class)
                .toProvider(Providers.of(configuration.getEndpoints().getSystem()));
        bind(CoordinationConfiguration.class).toProvider(Providers.of(configuration.getCoordination()));
    }
}
