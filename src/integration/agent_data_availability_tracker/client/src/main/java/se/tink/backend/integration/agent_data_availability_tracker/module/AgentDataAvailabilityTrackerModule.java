package se.tink.backend.integration.agent_data_availability_tracker.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import io.grpc.ManagedChannel;
import java.util.Objects;
import se.tink.backend.integration.agent_data_availability_tracker.client.AsAgentDataAvailabilityTrackerClient;
import se.tink.backend.integration.agent_data_availability_tracker.client.AsAgentDataAvailabilityTrackerClientImpl;
import se.tink.backend.integration.agent_data_availability_tracker.client.AsAgentDataAvailabilityTrackerClientMockImpl;
import se.tink.backend.integration.agent_data_availability_tracker.common.configuration.AgentDataAvailabilityTrackerConfiguration;

public class AgentDataAvailabilityTrackerModule extends AbstractModule {

    private final AgentDataAvailabilityTrackerConfiguration configuration;

    public AgentDataAvailabilityTrackerModule(
            AgentDataAvailabilityTrackerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {

        bind(AgentDataAvailabilityTrackerConfiguration.class).toInstance(configuration);
        bind(ManagedChannel.class).toProvider(TlsChannelProvider.class);

        if (Objects.nonNull(configuration) && configuration.isValid()) {
            bind(AsAgentDataAvailabilityTrackerClient.class)
                    .to(AsAgentDataAvailabilityTrackerClientImpl.class)
                    .in(Scopes.SINGLETON);
        } else {
            bind(AsAgentDataAvailabilityTrackerClient.class)
                    .to(AsAgentDataAvailabilityTrackerClientMockImpl.class)
                    .in(Scopes.SINGLETON);
        }
    }
}
