package se.tink.backend.aggregationcontroller.configuration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import se.tink.libraries.discovery.CoordinationModule;

public class AggregationControllerModuleFactory {

    public static Iterable<Module> build(AggregationControllerConfiguration configuration, JerseyEnvironment jersey) {
        return ImmutableList.<Module>builder().add(
                new AggregationControllerServiceModule(jersey),
                new AggregationControllerConfigurationModule(configuration),
                new CoordinationModule())
                .build();
    }
}
