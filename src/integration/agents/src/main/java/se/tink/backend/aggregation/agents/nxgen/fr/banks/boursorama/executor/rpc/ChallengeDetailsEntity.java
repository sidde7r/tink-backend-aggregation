package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ChallengeDetailsEntity {

    private static final String EMAIL_TYPE = "brs-otp-email";
    private static final String SMS_TYPE = "brs-otp-sms";
    private String type;

    private ChallengeParametersEntity parameters;

    public boolean isSmsOtp() {
        return SMS_TYPE.equals(type);
    }

    public boolean isEmailOtp() {
        return EMAIL_TYPE.equals(type);
    }
}
