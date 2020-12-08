package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeDataEntity {
    private String autoStartToken;

    public String getAutoStartToken() {
        return autoStartToken;
    }
}
