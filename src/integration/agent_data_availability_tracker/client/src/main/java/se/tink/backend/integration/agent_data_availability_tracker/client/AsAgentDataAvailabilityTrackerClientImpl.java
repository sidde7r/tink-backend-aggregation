package se.tink.backend.integration.agent_data_availability_tracker.client;

import com.google.inject.Inject;
import io.grpc.ManagedChannel;
import se.tink.backend.integration.agent_data_availability_tracker.common.client.AgentDataAvailabilityTrackerClient;
import se.tink.backend.integration.agent_data_availability_tracker.common.client.AgentDataAvailabilityTrackerClientImpl;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingMapSerializer;
import se.tink.libraries.dropwizard_lifecycle.ManagedSafeStop;

public class AsAgentDataAvailabilityTrackerClientImpl extends ManagedSafeStop
        implements AgentDataAvailabilityTrackerClient {

    private final AgentDataAvailabilityTrackerClientImpl agentDataAvailabilityTrackerClient;

    /** Construct client for accessing RouteGuide server at {@code host:port}. */
    @Inject
    private AsAgentDataAvailabilityTrackerClientImpl(final ManagedChannel channel) {
        this.agentDataAvailabilityTrackerClient =
                new AgentDataAvailabilityTrackerClientImpl(channel);
    }

    @Override
    public void beginStream() {
        agentDataAvailabilityTrackerClient.beginStream();
    }

    public void sendAccount(
            final String agent,
            final String provider,
            final String market,
            final TrackingMapSerializer serializer) {
        agentDataAvailabilityTrackerClient.sendAccount(agent, provider, market, serializer);
    }

    public void sendIdentityData(
            final String agent,
            final String provider,
            final String market,
            final TrackingMapSerializer identityDataSerializer) {
        agentDataAvailabilityTrackerClient.sendIdentityData(
                agent, provider, market, identityDataSerializer);
    }

    @Override
    public void endStreamBlocking() throws InterruptedException {
        agentDataAvailabilityTrackerClient.endStreamBlocking();
    }

    @Override
    public boolean sendingRealData() {
        return agentDataAvailabilityTrackerClient.sendingRealData();
    }

    @Override
    public void start() throws Exception {
        agentDataAvailabilityTrackerClient.start();
    }

    @Override
    public void doStop() throws Exception {
        agentDataAvailabilityTrackerClient.stop();
    }
}
