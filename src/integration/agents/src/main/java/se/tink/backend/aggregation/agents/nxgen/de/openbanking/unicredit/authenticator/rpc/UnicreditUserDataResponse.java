package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.entity.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.entity.ChosenScaMethodEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.entity.UnicreditConsentLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UnicreditUserDataResponse {

    private String consentStatus;
    private ChosenScaMethodEntity chosenScaMethod;
    private ChallengeDataEntity challengeData;

    @JsonProperty("_links")
    private UnicreditConsentLinksEntity links;

    public String getScaRedirect() {
        return links.getScaRedirect().getHref();
    }
}
