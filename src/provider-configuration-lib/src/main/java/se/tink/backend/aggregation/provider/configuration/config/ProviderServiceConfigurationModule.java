package se.tink.backend.aggregation.provider.configuration.config;

import com.google.inject.AbstractModule;

public class ProviderServiceConfigurationModule extends AbstractModule {
    private final ProviderServiceConfiguration configuration;

    public ProviderServiceConfigurationModule(ProviderServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(ProviderServiceConfiguration.class).toInstance(configuration);
        bind(PrometheusConfiguration.class).toInstance(configuration.getPrometheus());
    }
}
