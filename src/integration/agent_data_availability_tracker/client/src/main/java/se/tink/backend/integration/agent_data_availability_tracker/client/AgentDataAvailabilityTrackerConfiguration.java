package se.tink.backend.integration.agent_data_availability_tracker.client;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AgentDataAvailabilityTrackerConfiguration {

    private String host;
    private int port;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
