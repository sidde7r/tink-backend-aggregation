package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationResponse {
    @JsonProperty("_links")
    private LinksEntity links;

    private String scaStatus;
    private String authorizeId;
    private ChallengeDataEntity challengeData;
    private ScaMethodEntity chosenScaMethod;
    private String psuMessage;

    public String getCollectAuthUri() {
        return links.getScaStatus().getHref();
    }

    public String getScaStatus() {
        return scaStatus;
    }

    public ChallengeDataEntity getChallengeData() {
        return challengeData;
    }
}
