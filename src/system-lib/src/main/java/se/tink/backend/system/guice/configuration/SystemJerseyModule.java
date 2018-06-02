package se.tink.backend.system.guice.configuration;

import com.google.inject.AbstractModule;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import se.tink.libraries.jersey.guice.JerseyResourceRegistrar;
import se.tink.libraries.jersey.logging.AccessLoggingFilter;
import se.tink.libraries.jersey.logging.ResourceTimerFilterFactory;

public class SystemJerseyModule extends AbstractModule {
    private final JerseyEnvironment jersey;

    SystemJerseyModule(JerseyEnvironment jersey) {
        this.jersey = jersey;
    }

    @Override
    protected void configure() {
        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(jersey)
                .addFilterFactories(ResourceTimerFilterFactory.class)
                .addRequestFilters(AccessLoggingFilter.class)
                .addResponseFilters(AccessLoggingFilter.class)
                .bind();
    }
}
