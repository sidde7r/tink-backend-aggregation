package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AuthorisationResponse {
    private String authorisationId;
    private String scaStatus;
    private ChosenScaMethod chosenScaMethod;
    private ChallengeData challengeData;

    @JsonProperty("_links")
    public Links links;
}
