package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeDataEntity {
    private String otpFormat;
    private String otpMaxLength;
}
