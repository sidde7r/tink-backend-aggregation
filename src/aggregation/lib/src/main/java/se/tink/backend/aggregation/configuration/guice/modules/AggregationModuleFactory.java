package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import io.dropwizard.setup.Environment;
import se.tink.backend.aggregation.configuration.models.AggregationServiceConfiguration;
import se.tink.backend.integration.agent_data_availability_tracker.module.AgentDataAvailabilityTrackerModule;
import se.tink.libraries.discovery.CoordinationModule;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClientModule;
import se.tink.libraries.queue.sqs.configuration.SqsQueueConfiguration;
import se.tink.libraries.tracing.generic.configuration.GenericTracingModule;
import se.tink.libraries.tracing.lib.configuration.TracingModule;

public class AggregationModuleFactory {

    public static ImmutableList<Module> build(
            AggregationServiceConfiguration configuration, Environment environment) {
        if (configuration.isDecoupledMode()) {
            return ImmutableList.of(new AggregationDecoupledModule(configuration, environment));
        }
        if (configuration.isDevelopmentMode()) {
            return buildForDevelopment(configuration, environment).build();
        }

        return buildForProduction(configuration, environment).build();
    }

    private static ImmutableList.Builder<Module> baseBuilder(
            AggregationServiceConfiguration configuration, Environment environment) {
        return new ImmutableList.Builder<Module>()
                .add(new AggregationCommonModule())
                .add(new CoordinationModule())
                .add(new AgentFactoryModule())
                .add(new AgentWorkerCommandModule())
                .add(new AggregationConfigurationModule(configuration))
                .add(new AggregationHealthChecksModule(configuration))
                .add(new AggregationModule(configuration, environment.jersey()))
                .add(
                        new AgentDataAvailabilityTrackerModule(
                                configuration
                                        .getAgentsServiceConfiguration()
                                        .getAgentDataAvailabilityTrackerConfiguration()))
                .add(new QueueModule(configuration.getSqsQueueConfiguration()))
                .add(
                        new EventProducerServiceClientModule(
                                configuration
                                        .getEndpoints()
                                        .getEventProducerServiceConfiguration()))
                // TODO: Switch to TracingModuleFactory once we've solved cross-cluster jaeger setup
                .add(new TracingModule())
                .add(new GenericTracingModule());
    }

    private static ImmutableList.Builder<Module> buildForDevelopment(
            AggregationServiceConfiguration configuration, Environment environment) {
        return baseBuilder(configuration, environment)
                .add(
                        new AggregationDevelopmentRepositoryModule(
                                configuration.getDatabase(),
                                configuration.getDevelopmentConfiguration()));
    }

    private static ImmutableList.Builder<Module> buildForProduction(
            AggregationServiceConfiguration configuration, Environment environment) {
        return baseBuilder(configuration, environment)
                .add(new AggregationRepositoryModule(configuration.getDatabase()));
    }
}
