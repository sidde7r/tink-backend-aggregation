package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class KeyCardChallengeEntity {
    private String nemidChallenge;
    private String nemidResponse;
    private boolean bindSca = true;
    private String scaInstanceId;

    public KeyCardChallengeEntity(String nemidChallenge, String nemidResponse) {
        this.nemidChallenge = nemidChallenge;
        this.nemidResponse = nemidResponse;
        bindSca = true;
        scaInstanceId = "";
    }
}
