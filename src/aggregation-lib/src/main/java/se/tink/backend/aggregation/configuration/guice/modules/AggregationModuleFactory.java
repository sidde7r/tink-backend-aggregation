package se.tink.backend.aggregation.guice.configuration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import io.dropwizard.setup.Environment;
import se.tink.backend.aggregation.configuration.AggregationServiceConfiguration;
import se.tink.backend.aggregation.configuration.guice.modules.AgentWorkerCommandModule;
import se.tink.backend.aggregation.configuration.guice.modules.AggregationCommonModule;
import se.tink.backend.aggregation.configuration.guice.modules.AggregationConfigurationModule;
import se.tink.backend.aggregation.configuration.guice.modules.AggregationDevelopmentMultiClientRepositoryModule;
import se.tink.backend.aggregation.configuration.guice.modules.AggregationDevelopmentSingleClientRepositoryModule;
import se.tink.backend.aggregation.configuration.guice.modules.AggregationModule;
import se.tink.backend.aggregation.configuration.guice.modules.AggregationMultiClientRepositoryModule;
import se.tink.backend.aggregation.configuration.guice.modules.AggregationSingleClientRepositoryModule;
import se.tink.backend.aggregation.configuration.guice.modules.QueueModule;
import se.tink.libraries.discovery.CoordinationModule;

public class AggregationModuleFactory {

    public static ImmutableList<Module> build(AggregationServiceConfiguration configuration, Environment environment) {
        if (configuration.isDevelopmentMode()) {
            return buildForDevelopment(configuration, environment).build();
        }

        return buildForProduction(configuration, environment).build();
    }

    private static ImmutableList.Builder<Module> baseBuilder(AggregationServiceConfiguration configuration,
                                                             Environment environment) {
        return new ImmutableList.Builder<Module>()
                .add(new AggregationCommonModule())
                .add(new CoordinationModule())
                .add(new AgentWorkerCommandModule())
                .add(new AggregationConfigurationModule(configuration))
                .add(new AggregationModule(configuration, environment.jersey()))
                .add(new QueueModule(configuration.getSqsQueueConfiguration(), environment.lifecycle()));
    }

    private static ImmutableList.Builder<Module> buildForDevelopment(AggregationServiceConfiguration configuration,
            Environment environment) {

        if (configuration.isMultiClientDevelopment()){
            return baseBuilder(configuration, environment).add(
                    new AggregationDevelopmentMultiClientRepositoryModule(configuration.getDatabase()));
        }

        return baseBuilder(configuration, environment).add(
                new AggregationDevelopmentSingleClientRepositoryModule(configuration.getDatabase(),
                        configuration.getDevelopmentConfiguration()));
    }

    private static ImmutableList.Builder<Module> buildForProduction(AggregationServiceConfiguration configuration,
            Environment environment) {

        if (configuration.isMultiClientDevelopment()) {
            return baseBuilder(configuration, environment).add(
                    new AggregationMultiClientRepositoryModule(configuration.getDatabase()));
        }
        
        return baseBuilder(configuration, environment).add(
                new AggregationSingleClientRepositoryModule(configuration.getDatabase()));
    }
}
