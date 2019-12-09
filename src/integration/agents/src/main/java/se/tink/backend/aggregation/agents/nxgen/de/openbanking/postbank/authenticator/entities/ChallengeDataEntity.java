package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities;

import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeDataEntity {

    private String otpMaxLength;

    private String otpFormat;

    public int getOtpMaxLength() {
        return Strings.isNullOrEmpty(otpMaxLength) ? Integer.parseInt(otpMaxLength) : 0;
    }

    public String getOtpFormat() {
        return otpFormat;
    }
}
