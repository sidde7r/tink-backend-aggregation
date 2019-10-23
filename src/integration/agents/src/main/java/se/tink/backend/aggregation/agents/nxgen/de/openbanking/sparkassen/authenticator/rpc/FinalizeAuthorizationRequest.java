package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinalizeAuthorizationRequest {

    private String scaAuthenticationData;

    @JsonCreator
    public FinalizeAuthorizationRequest(
            @JsonProperty("scaAuthenticationData") String scaAuthenticationData) {
        this.scaAuthenticationData = scaAuthenticationData;
    }
}
