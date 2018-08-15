package se.tink.backend.aggregation.guice.configuration;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.guice.configuration.CommonModule;
import se.tink.backend.guice.configuration.ConfigurationModule;
import se.tink.libraries.discovery.CoordinationModule;

public class AggregationModuleFactory {
    public static ImmutableList<Module> build(ServiceConfiguration configuration,
            Environment environment) {
        return ImmutableList.of(
                new CommonModule(),
                new CoordinationModule(),
                new AggregationRepositoryModule(configuration.getDatabase()),
                new ConfigurationModule(configuration),
                new AggregationModule(configuration, environment.jersey()),
                new QueueModule(configuration.getSqsQueueConfiguration(), environment.lifecycle())
        );
    }
}
