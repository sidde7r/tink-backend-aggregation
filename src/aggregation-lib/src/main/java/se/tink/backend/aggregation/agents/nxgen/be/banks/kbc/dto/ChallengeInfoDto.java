package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeInfoDto {
    private TypeValuePair generalText;

    public TypeValuePair getGeneralText() {
        return generalText;
    }
}
