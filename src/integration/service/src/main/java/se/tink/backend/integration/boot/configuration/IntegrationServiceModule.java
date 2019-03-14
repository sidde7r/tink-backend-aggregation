package se.tink.backend.integration.boot.configuration;

import com.google.inject.AbstractModule;

public class IntegrationServiceModule extends AbstractModule {

    private Configuration configuration;
    private SensitiveConfiguration sensitiveConfiguration;

    public IntegrationServiceModule(Configuration configuration, SensitiveConfiguration sensitiveConfiguration) {
        this.configuration = configuration;
        this.sensitiveConfiguration = sensitiveConfiguration;
    }

    @Override
    protected void configure() {
        bind(Configuration.class).toInstance(configuration);
        bind(SensitiveConfiguration.class).toInstance(sensitiveConfiguration);
    }
}
