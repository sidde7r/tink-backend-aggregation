package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities.ChallengeData;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities.Links;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities.ScaMethod;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AuthorisationResponse {

    @JsonProperty("_links")
    private Links links;

    private ChallengeData challengeData;
    private ScaMethod chosenScaMethod;
    private String scaStatus;
    private List<ScaMethod> scaMethods;
}
