package se.tink.backend.aggregation.guice.configuration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import io.dropwizard.setup.Environment;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.guice.configuration.CommonModule;
import se.tink.backend.guice.configuration.ConfigurationModule;
import se.tink.libraries.discovery.CoordinationModule;

public class AggregationModuleFactory {

    public static ImmutableList<Module> build(ServiceConfiguration configuration, Environment environment) {
        if (configuration.isDevelopmentMode()) {
            return buildForDevelopment(configuration, environment).build();
        }

        return buildForProduction(configuration, environment).build();
    }

    private static ImmutableList.Builder<Module> baseBuilder(ServiceConfiguration configuration,
                                                             Environment environment) {
        return new ImmutableList.Builder<Module>()
                .add(new CommonModule())
                .add(new CoordinationModule())
                .add(new ConfigurationModule(configuration))
                .add(new AggregationModule(configuration, environment.jersey()))
                .add(new QueueModule(configuration.getSqsQueueConfiguration(), environment.lifecycle()));
    }

    private static ImmutableList.Builder<Module> buildForDevelopment(ServiceConfiguration configuration,
            Environment environment) {
        return baseBuilder(configuration, environment).add(
                new AggregationDevelopmentSingleClientRepositoryModule(configuration.getDatabase(),
                        configuration.getDevelopmentConfiguration()));
    }

    private static ImmutableList.Builder<Module> buildForProduction(ServiceConfiguration configuration,
            Environment environment) {
        return baseBuilder(configuration, environment).add(
                new AggregationSingleClientRepositoryModule(configuration.getDatabase()));
    }
}
