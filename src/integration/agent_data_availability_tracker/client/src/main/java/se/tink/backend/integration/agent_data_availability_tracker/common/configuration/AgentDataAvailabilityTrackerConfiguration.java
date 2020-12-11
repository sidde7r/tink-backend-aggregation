package se.tink.backend.integration.agent_data_availability_tracker.common.configuration;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AgentDataAvailabilityTrackerConfiguration {

    private String host;
    private int port;
    private String caPath;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getCaPath() {
        return caPath;
    }

    public boolean isValid() {
        return !Strings.isNullOrEmpty(host) && port != 0;
    }
}
