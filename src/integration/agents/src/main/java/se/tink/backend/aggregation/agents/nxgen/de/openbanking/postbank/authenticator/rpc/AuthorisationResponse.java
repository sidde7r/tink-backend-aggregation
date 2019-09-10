package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorisationResponse {

    @JsonProperty("challengeData")
    private ChallengeDataEntity challengeDataEntity;

    @JsonProperty("chosenScaMethod")
    private ScaMethodEntity chosenScaMethodEntity;

    private String scaStatus;

    private List<ScaMethodEntity> scaMethods;

    @JsonProperty("_links")
    private LinksEntity linksEntity;

    public LinksEntity getLinksEntity() {
        return linksEntity;
    }

    public String getScaStatus() {
        return scaStatus;
    }

    public List<ScaMethodEntity> getScaMethods() {
        return scaMethods;
    }

    public ChallengeDataEntity getChallengeDataEntity() {
        return challengeDataEntity;
    }

    public ScaMethodEntity getChosenScaMethodEntity() {
        return chosenScaMethodEntity;
    }
}
