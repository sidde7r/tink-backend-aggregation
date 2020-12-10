package se.tink.backend.integration.agent_data_availability_tracker.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import io.grpc.ManagedChannel;
import java.util.Objects;
import se.tink.backend.integration.agent_data_availability_tracker.client.AsAgentDataAvailabilityTrackerClientImpl;
import se.tink.backend.integration.agent_data_availability_tracker.common.client.AgentDataAvailabilityTrackerClient;
import se.tink.backend.integration.agent_data_availability_tracker.common.client.AgentDataAvailabilityTrackerClientMockImpl;
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
            bind(AgentDataAvailabilityTrackerClient.class)
                    .to(AsAgentDataAvailabilityTrackerClientImpl.class)
                    .in(Scopes.SINGLETON);
        } else {
            bind(AgentDataAvailabilityTrackerClient.class)
                    .to(AgentDataAvailabilityTrackerClientMockImpl.class)
                    .in(Scopes.SINGLETON);
        }
    }
}
