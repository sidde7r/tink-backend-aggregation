package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.dropwizard.setup.Environment;
import se.tink.backend.aggregation.configuration.guice.modules.agentcapabilities.AgentCapabilitiesModule;
import se.tink.backend.aggregation.configuration.models.AggregationServiceConfiguration;
import se.tink.libraries.discovery.CoordinationModule;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClientModule;
import se.tink.libraries.events.guice.EventsModule;
import se.tink.libraries.events.guice.configuration.EventSubmitterConfiguration;
import se.tink.libraries.queue.sqs.configuration.SqsQueueConfiguration;
import se.tink.libraries.tracing.configuration.guice.TracingModuleFactory;
import se.tink.libraries.unleash.module.UnleashModule;
import se.tink.libraries.unleash.strategies.ServiceType;

public class AggregationModuleFactory {

    public static ImmutableList<Module> build(
            AggregationServiceConfiguration configuration, Environment environment) {
        if (configuration.isDecoupledMode()) {
            return buildForDecoupledMode(configuration, environment);
        }
        if (configuration.isDevelopmentMode()) {
            return buildForDevelopment(configuration, environment).build();
        }
        if (configuration.isSystemTestMode()) {
            return ImmutableList.of(
                    Modules.override(buildForDecoupledMode(configuration, environment))
                            .with(new AggregationSystemTestModule()));
        }

        return buildForProduction(configuration, environment).build();
    }

    private static ImmutableList<Module> buildForDecoupledMode(
            AggregationServiceConfiguration configuration, Environment environment) {
        return baseBuilder(environment, configuration)
                .add(new AggregationDecoupledModule(configuration, environment))
                .build();
    }

    private static ImmutableList.Builder<Module> baseBuilder(
            Environment environment, AggregationServiceConfiguration configuration) {
        Builder<Module> builder =
                new Builder<Module>().add(new AgentCapabilitiesModule(environment.jersey()));
        if (configuration.getJaegerConfig() != null) {
            builder.addAll(TracingModuleFactory.getModules(configuration.getJaegerConfig()));
        }
        return builder;
    }

    public static ImmutableList.Builder<Module> productionBuilder(
            AggregationServiceConfiguration configuration, Environment environment) {
        return baseBuilder(environment, configuration)
                .add(new AggregationCommonModule())
                .add(new CoordinationModule())
                .add(new AgentFactoryModule())
                .add(new AgentWorkerCommandModule())
                .add(new AggregationConfigurationModule(configuration))
                .add(new AggregationHealthChecksModule(configuration))
                .add(new AggregationModule(configuration, environment.jersey()))
                .add(getQueueModule(configuration.getRegularSqsQueueConfiguration()))
                .add(
                        new EventProducerServiceClientModule(
                                configuration
                                        .getEndpoints()
                                        .getEventProducerServiceConfiguration()))
                .add(
                        new EventsModule(
                                EventSubmitterConfiguration.of(
                                        "aggregation",
                                        configuration
                                                .getEndpoints()
                                                .getEventProducerServiceConfiguration())))
                .add(
                        new UnleashModule(
                                configuration.getUnleashConfiguration(), ServiceType.AGGREGATION));

        // TODO: Switch to TracingModuleFactory once we've solved cross-cluster jaeger setup
    }

    private static AbstractModule getQueueModule(SqsQueueConfiguration sqsQueueConfiguration) {
        return sqsQueueConfiguration.isEnabled() ? new SqsQueueModule() : new FakeQueueModule();
    }

    private static ImmutableList.Builder<Module> buildForDevelopment(
            AggregationServiceConfiguration configuration, Environment environment) {
        return productionBuilder(configuration, environment)
                .add(
                        new AggregationDevelopmentRepositoryModule(
                                configuration.getDatabase(),
                                configuration.getDevelopmentConfiguration()));
    }

    private static ImmutableList.Builder<Module> buildForProduction(
            AggregationServiceConfiguration configuration, Environment environment) {
        return productionBuilder(configuration, environment)
                .add(new AggregationRepositoryModule(configuration.getDatabase()));
    }
}
