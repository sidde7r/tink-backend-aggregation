package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.entities.OutputEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class GenerateOtpChallengeResponse {
    private OutputEntity output;

    public String getChallenge() {
        return output.getChallenge();
    }
}
