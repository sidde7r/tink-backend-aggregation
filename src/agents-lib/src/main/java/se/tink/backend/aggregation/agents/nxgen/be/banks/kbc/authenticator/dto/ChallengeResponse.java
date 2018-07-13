package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.HeaderResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeResponse extends HeaderResponse {
    private TypeValuePair challenge;
    private TypeValuePair challengeTimeout;

    public TypeValuePair getChallenge() {
        return challenge;
    }

    public TypeValuePair getChallengeTimeout() {
        return challengeTimeout;
    }
}
