package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaConstants.Avain;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitiateChallengeRequest {

    private final String codeChallengeType;

    public InitiateChallengeRequest() {
        this.codeChallengeType = Avain.CHALLENGE_TYPE;
    }

    public String getCodeChallengeType() {
        return codeChallengeType;
    }
}
