package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AuthMethodSelectionResponse {

    @JsonProperty("status")
    private String scaStatus;

    @JsonProperty("chosen_sca_method")
    private String chosenScaMethod;

    @JsonProperty("challenge_data")
    private ChallengeDataEntity challengeData;

    @JsonProperty("_links")
    private LinksEntity links;
}
