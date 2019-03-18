package se.tink.backend.nasa.boot.configuration;

import com.google.inject.AbstractModule;

public class NASAServiceModule extends AbstractModule {
    private Configuration configuration;
    private SensitiveConfiguration sensitiveConfiguration;

    public NASAServiceModule(Configuration configuration, SensitiveConfiguration sensitiveConfiguration) {
        this.configuration = configuration;
        this.sensitiveConfiguration = sensitiveConfiguration;
    }

    @Override
    protected void configure() {
        bind(Configuration.class).toInstance(configuration);
        bind(SensitiveConfiguration.class).toInstance(sensitiveConfiguration);
    }
}
