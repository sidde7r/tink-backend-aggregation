package se.tink.backend.integration.agent_data_availability_tracker.client;

import se.tink.backend.integration.agent_data_availability_tracker.common.TrackingMapSerializer;

public class AgentDataAvailabilityTrackerClientMockImpl
        implements AgentDataAvailabilityTrackerClient {

    @Override
    public void beginStream() {}

    @Override
    public void sendAccount(
            String agent, String provider, String market, TrackingMapSerializer serializer) {}

    @Override
    public void sendIdentityData(
            final String agent,
            final String provider,
            final String market,
            final TrackingMapSerializer identityDataSerializer) {}

    @Override
    public void endStreamBlocking() throws InterruptedException {}

    @Override
    public boolean sendingRealData() {
        return false;
    }
}
