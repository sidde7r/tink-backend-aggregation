package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaStatusResponse {
    @JsonProperty private int scaStatus;
    @JsonProperty private String ticket;
    @JsonProperty private String rememberMeToken;
    @JsonProperty private int timeoutInSeconds;
    @JsonProperty private int resultCode;
    @JsonProperty private boolean isSameDevice;

    public int getScaStatus() {
        return scaStatus;
    }

    public String getTicket() {
        return ticket;
    }

    public String getRememberMeToken() {
        return rememberMeToken;
    }
}
