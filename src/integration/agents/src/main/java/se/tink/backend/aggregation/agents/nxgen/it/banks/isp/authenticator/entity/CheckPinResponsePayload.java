package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CheckPinResponsePayload {

    @JsonProperty private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }
}
