package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ScaAuthorizationLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitAuthorizationResponse {

    @JsonProperty("_links")
    private ScaAuthorizationLinksEntity links;

    private String scaStatus;
    private String authorisationId;
    private List<ScaMethodEntity> scaMethods;
    private ChallengeDataEntity challengeData;
    private String psuMessage;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setScaMethods(List<ScaMethodEntity> scaMethods) {
        this.scaMethods = scaMethods;
    }

    public List<ScaMethodEntity> getScaMethods() {
        return scaMethods;
    }

    public String getAuthorisationId() {
        return authorisationId;
    }

    public ChallengeDataEntity getChallengeData() {
        return challengeData;
    }
}
