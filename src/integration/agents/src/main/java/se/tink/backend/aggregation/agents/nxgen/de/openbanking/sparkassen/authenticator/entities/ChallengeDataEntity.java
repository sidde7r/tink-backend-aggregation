package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeDataEntity {

    private String image;
    private Integer otpMaxLength;
    private String otpFormat;
    private String additionalInformation;

    public Integer getOtpMaxLength() {
        return otpMaxLength;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }
}
