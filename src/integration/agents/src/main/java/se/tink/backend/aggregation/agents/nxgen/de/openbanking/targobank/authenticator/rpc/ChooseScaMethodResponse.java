package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.entities.ScaAuthenticationLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChooseScaMethodResponse {
    private ScaMethodEntity chosenScaMethod;
    private ChallengeDataEntity challengeData;
    private String scaStatus;

    @JsonProperty("_links")
    private ScaAuthenticationLinksEntity link;

    public ScaMethodEntity getChosenScaMethod() {
        return chosenScaMethod;
    }
}
