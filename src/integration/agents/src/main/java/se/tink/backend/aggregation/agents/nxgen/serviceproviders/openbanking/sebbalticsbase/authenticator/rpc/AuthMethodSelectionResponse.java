package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthMethodSelectionResponse {

    @JsonProperty("sca_status")
    private String scaStatus;

    @JsonProperty("chosen_sca_method")
    private String chosenScaMethod;

    @JsonProperty("challenge_data")
    private ChallengeDataEntity challengeData;

    private LinksEntity links;
}
