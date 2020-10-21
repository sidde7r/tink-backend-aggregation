package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities.StatusEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class VerifyChallengeResponse {

    private StatusEntity status;

    private int triesLeft;

    public StatusEntity getStatus() {
        return status;
    }

    public int getTriesLeft() {
        return triesLeft;
    }
}
