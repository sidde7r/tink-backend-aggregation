package se.tink.backend.integration.agent_data_availability_tracker.client;

import se.tink.backend.integration.agent_data_availability_tracker.common.TrackingMapSerializer;

public interface AgentDataAvailabilityTrackerClient {

    void beginStream();

    void sendAccount(
            final String agent,
            final String provider,
            final String market,
            final TrackingMapSerializer serializer);

    void sendIdentityData(
            final String agent,
            final String provider,
            final String market,
            final TrackingMapSerializer identityDataSerializer);

    void endStreamBlocking() throws InterruptedException;

    boolean sendingRealData();
}
