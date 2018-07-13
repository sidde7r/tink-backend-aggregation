package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignChallengeResponse {
    private TypeValuePair challenge;
    private ChallengeInfoDto challengeInfo;
    private SigningIdHeaderDto header;

    public TypeValuePair getChallenge() {
        return challenge;
    }

    public ChallengeInfoDto getChallengeInfoDto() {
        return challengeInfo;
    }

    public SigningIdHeaderDto getHeader() {
        return header;
    }
}
