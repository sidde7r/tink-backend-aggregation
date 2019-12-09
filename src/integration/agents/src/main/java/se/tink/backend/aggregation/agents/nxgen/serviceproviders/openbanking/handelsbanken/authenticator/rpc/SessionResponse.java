package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities.SessionLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionResponse {

    @JsonProperty("_links")
    private SessionLinksEntity sessionLinksEntity;

    @JsonProperty("auto_start_token")
    private String autoStartToken;

    @JsonProperty("sleep_time")
    private int sleepTime;

    public SessionLinksEntity getLinks() {
        return sessionLinksEntity;
    }

    public String getAutoStartToken() {
        return autoStartToken;
    }

    public int getSleepTime() {
        return sleepTime;
    }
}
