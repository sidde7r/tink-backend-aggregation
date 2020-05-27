package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeSolutionRequest {
    private String signature;

    @JsonProperty("user_identifier")
    private String userIdentifier;

    @JsonIgnore
    public ChallengeSolutionRequest(String signature, String userId) {
        this.signature = signature;
        this.userIdentifier = userId;
    }
}
