package se.tink.backend.product.execution.configuration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import se.tink.libraries.discovery.CoordinationModule;

public class ProductExecutorModuleFactory {
    public static Iterable<Module> build(ProductExecutorConfiguration configuration, JerseyEnvironment environment) {
        return ImmutableList.<Module
                >builder()
                .add(
                        new ProductExecutorConfigurationModule(configuration),
                        new CoordinationModule(),
                        new ServiceModule(environment)
                )
                .build();
    }
}
