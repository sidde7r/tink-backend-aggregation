package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetConsentResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    private ChallengeDataEntity challengeData;
    private ScaMethodEntity chosenScaMethod;
    private String consentId;
    private String consentStatus;
    private String message;
    private List<ScaMethodEntity> scaMethods;

    public String getConsentId() {
        return consentId;
    }
}
