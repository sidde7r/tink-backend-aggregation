package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.dto;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.SignValidationResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SigningChallengeUcrResponse extends SignValidationResponse {
    private TypeValuePair challenge;
    private List<TypeValuePair> trxData;
    private ChallengeInfoDto challengeInfo;

    public TypeValuePair getChallenge() {
        return challenge;
    }
}
