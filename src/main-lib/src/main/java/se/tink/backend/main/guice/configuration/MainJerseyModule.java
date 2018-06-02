package se.tink.backend.main.guice.configuration;

import com.google.inject.AbstractModule;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import se.tink.backend.api.DeviceService;
import se.tink.backend.api.InvestmentService;
import se.tink.backend.api.TrackingService;
import se.tink.backend.api.TransferService;
import se.tink.libraries.jersey.guice.JerseyResourceRegistrar;
import se.tink.libraries.jersey.logging.AccessLoggingFilter;
import se.tink.libraries.jersey.logging.PublicAPIHTTPHeadersFilter;
import se.tink.libraries.jersey.logging.ResourceTimerFilterFactory;

public class MainJerseyModule extends AbstractModule {
    private final JerseyEnvironment jersey;

    MainJerseyModule(JerseyEnvironment jersey) {
        this.jersey = jersey;
    }

    @Override
    protected void configure() {
        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(jersey)
                .addResources(DeviceService.class, TransferService.class, TrackingService.class, InvestmentService.class)
                .addFilterFactories(ResourceTimerFilterFactory.class)
                .addRequestFilters(AccessLoggingFilter.class)
                .addResponseFilters(AccessLoggingFilter.class, PublicAPIHTTPHeadersFilter.class)
                .bind();
    }
}
