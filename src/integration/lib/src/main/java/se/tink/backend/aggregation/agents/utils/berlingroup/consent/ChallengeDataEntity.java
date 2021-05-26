package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ChallengeDataEntity {
    private String image;
    private Integer otpMaxLength;
    private OtpFormat otpFormat;
    private String additionalInformation;
}
