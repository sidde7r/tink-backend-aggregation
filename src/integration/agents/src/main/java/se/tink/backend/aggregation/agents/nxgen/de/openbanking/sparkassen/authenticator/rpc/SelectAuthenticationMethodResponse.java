package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ScaAuthorizationLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SelectAuthenticationMethodResponse {

    private String scaStatus;
    private ScaMethodEntity chosenScaMethod;
    private ChallengeDataEntity challengeData;
    private ScaAuthorizationLinksEntity links;
    private String psuMessage;

    public ChallengeDataEntity getChallengeData() {
        return challengeData;
    }

    public ScaMethodEntity getChosenScaMethod() {
        return chosenScaMethod;
    }
}
