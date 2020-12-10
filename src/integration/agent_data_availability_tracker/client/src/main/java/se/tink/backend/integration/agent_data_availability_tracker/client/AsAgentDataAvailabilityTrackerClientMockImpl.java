package se.tink.backend.integration.agent_data_availability_tracker.client;

import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingMapSerializer;

public class AsAgentDataAvailabilityTrackerClientMockImpl
        extends AsAgentDataAvailabilityTrackerClient {

    @Override
    public void beginStream() {
        // noop
    }

    @Override
    public void sendAccount(
            String agent, String provider, String market, TrackingMapSerializer serializer) {
        // noop
    }

    @Override
    public void sendIdentityData(
            final String agent,
            final String provider,
            final String market,
            final TrackingMapSerializer identityDataSerializer) {
        // noop
    }

    @Override
    public void endStreamBlocking() throws InterruptedException {
        // noop
    }

    @Override
    public boolean sendingRealData() {
        return false;
    }

    @Override
    public void doStop() throws Exception {
        // noop
    }

    @Override
    public void start() throws Exception {
        // noop
    }
}
