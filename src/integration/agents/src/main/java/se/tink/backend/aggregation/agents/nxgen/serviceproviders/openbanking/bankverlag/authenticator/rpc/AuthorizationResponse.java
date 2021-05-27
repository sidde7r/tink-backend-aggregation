package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.entities.ScaAuthorizationLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AuthorizationResponse {

    @JsonProperty("_links")
    private ScaAuthorizationLinksEntity links;

    private String scaStatus;
    private String authorisationId;
    private List<ScaMethodEntity> scaMethods;
    @Setter private ScaMethodEntity chosenScaMethod;
    private ChallengeDataEntity challengeData;
    private String psuMessage;

    public String getScaStatus() {
        return scaStatus;
    }

    public String getAuthorisationId() {
        return authorisationId;
    }

    public List<ScaMethodEntity> getScaMethods() {
        return scaMethods;
    }

    public ScaMethodEntity getChosenScaMethod() {
        return chosenScaMethod;
    }

    public ChallengeDataEntity getChallengeData() {
        return challengeData;
    }
}
