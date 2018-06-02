package se.tink.backend.aggregationcontroller.configuration;

import com.google.inject.AbstractModule;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import se.tink.backend.aggregationcontroller.resources.AggregationControllerAggregationServiceResource;
import se.tink.backend.aggregationcontroller.resources.AggregationControllerCredentialsServiceResource;
import se.tink.backend.aggregationcontroller.resources.AggregationControllerProcessServiceResource;
import se.tink.backend.aggregationcontroller.resources.AggregationControllerProviderServiceResource;
import se.tink.backend.aggregationcontroller.resources.AggregationControllerServiceResource;
import se.tink.backend.aggregationcontroller.resources.AggregationControllerUpdateServiceResource;
import se.tink.backend.aggregationcontroller.v1.api.AggregationControllerAggregationService;
import se.tink.backend.aggregationcontroller.v1.api.AggregationControllerCredentialsService;
import se.tink.backend.aggregationcontroller.v1.api.AggregationControllerProcessService;
import se.tink.backend.aggregationcontroller.v1.api.AggregationControllerProviderService;
import se.tink.backend.aggregationcontroller.v1.api.AggregationControllerService;
import se.tink.backend.aggregationcontroller.v1.api.AggregationControllerUpdateService;
import se.tink.libraries.http.client.RequestTracingFilter;
import se.tink.libraries.jersey.guice.JerseyResourceRegistrar;
import se.tink.libraries.jersey.logging.AccessLoggingFilter;
import se.tink.libraries.jersey.logging.ResourceTimerFilterFactory;

public class AggregationControllerServiceModule extends AbstractModule {

    private final JerseyEnvironment jersey;

    AggregationControllerServiceModule(JerseyEnvironment jersey) {
        this.jersey = jersey;
    }

    @Override
    protected void configure() {
        bind(AggregationControllerCredentialsService.class).to(AggregationControllerCredentialsServiceResource.class);
        bind(AggregationControllerAggregationService.class).to(AggregationControllerAggregationServiceResource.class);
        bind(AggregationControllerUpdateService.class).to(AggregationControllerUpdateServiceResource.class);
        bind(AggregationControllerProcessService.class).to(AggregationControllerProcessServiceResource.class);
        bind(AggregationControllerService.class).to(AggregationControllerServiceResource.class);
        bind(AggregationControllerProviderService.class).to(AggregationControllerProviderServiceResource.class);

        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(jersey)
                .addResources(
                        AggregationControllerCredentialsService.class,
                        AggregationControllerAggregationService.class,
                        AggregationControllerProcessService.class,
                        AggregationControllerUpdateService.class,
                        AggregationControllerService.class,
                        AggregationControllerProviderService.class
                )
                .addFilterFactories(ResourceTimerFilterFactory.class)
                .addRequestFilters(RequestTracingFilter.class, AccessLoggingFilter.class)
                .addResponseFilters(RequestTracingFilter.class, AccessLoggingFilter.class)
                .bind();
    }
}
