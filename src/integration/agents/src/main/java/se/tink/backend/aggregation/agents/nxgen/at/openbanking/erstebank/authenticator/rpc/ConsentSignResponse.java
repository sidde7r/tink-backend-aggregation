package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.entities.ChosenScaMethodEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentSignResponse {
    private String authorisationId;
    private ChallengeDataEntity challengeData;
    private ChosenScaMethodEntity chosenScaMethod;
    private String psuMessage;
    private List<ScaMethodEntity> scaMethods;
    private String scaStatus;

    public String getAuthorisationId() {
        return authorisationId;
    }
}
