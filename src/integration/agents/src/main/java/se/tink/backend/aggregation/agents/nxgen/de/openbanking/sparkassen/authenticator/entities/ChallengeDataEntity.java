package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ChallengeDataEntity {

    private String image;
    private Integer otpMaxLength;
    private String otpFormat;
    private String additionalInformation;
}
