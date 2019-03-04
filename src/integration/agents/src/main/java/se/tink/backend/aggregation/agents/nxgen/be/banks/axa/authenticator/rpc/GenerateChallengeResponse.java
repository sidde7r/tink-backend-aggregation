package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.OutputEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class GenerateChallengeResponse {
    private OutputEntity output;

    public String getChallenge() {
        return output.getChallenge();
    }

    public String getActivationPassword() {
        return output.getActivationPassword();
    }
}
