package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeDataEntity {
    private String code;
    private String autoStartToken;

    public String getAutoStartToken() {
        return autoStartToken;
    }
}
