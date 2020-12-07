package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationResponse {
    @JsonProperty("_links")
    private LinksEntity links;

    private String scaStatus;
    private ChallengeDataEntity challengeData;

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
