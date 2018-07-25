package se.tink.backend.aggregation.provider.configuration.config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import se.tink.backend.aggregation.provider.configuration.api.ProviderService;
import se.tink.backend.aggregation.provider.configuration.controllers.ProviderServiceController;
import se.tink.backend.aggregation.provider.configuration.resources.ProviderServiceResource;
import se.tink.libraries.jersey.guice.JerseyResourceRegistrar;
import se.tink.libraries.jersey.logging.AccessLoggingFilter;
import se.tink.libraries.jersey.logging.ResourceTimerFilterFactory;

public class ProviderServiceModule extends AbstractModule {
    private final JerseyEnvironment jersey;

    ProviderServiceModule(JerseyEnvironment jersey) {
        this.jersey = jersey;
    }

    @Override
    protected void configure() {
        bind(ProviderService.class).to(ProviderServiceResource.class).in(Scopes.SINGLETON);
        bind(ProviderServiceController.class).in(Scopes.SINGLETON);

        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(jersey)
                .addFilterFactories(ResourceTimerFilterFactory.class)
                .addRequestFilters(AccessLoggingFilter.class)
                .addResponseFilters(AccessLoggingFilter.class)
                .addResources(ProviderService.class)
                .bind();
    }
}
