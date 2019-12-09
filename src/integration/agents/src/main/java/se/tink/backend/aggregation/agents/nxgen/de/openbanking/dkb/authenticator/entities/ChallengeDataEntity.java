package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeDataEntity {
    private String additionalInformation;
    private String data;
    private String image;
    private String imageLink;
    private String otpFormat;
    private Long otpMaxLength;
}
