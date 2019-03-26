package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.sidentity.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PollResponse {

    @JsonProperty("secondFactorStatus")
    private String secondFactorStatus;

    @JsonProperty("pollingIntervalMs")
    private int pollingIntervalMs;

    public String getSecondFactorStatus() {
        return secondFactorStatus;
    }

    public int getPollingIntervalMs() {
        return pollingIntervalMs;
    }
}
