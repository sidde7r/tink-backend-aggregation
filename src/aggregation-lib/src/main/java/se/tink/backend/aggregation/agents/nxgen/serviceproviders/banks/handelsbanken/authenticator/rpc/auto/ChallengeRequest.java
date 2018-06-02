package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeRequest {

    private String cnonce;

    public ChallengeRequest setCnonce(String cnonce) {
        this.cnonce = cnonce;
        return this;
    }
}
