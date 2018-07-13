package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OtpChallengeAuthenticationRequest {
    private final String otpCode;

    private OtpChallengeAuthenticationRequest(String otpCode) {
        this.otpCode = otpCode;
    }

    public static OtpChallengeAuthenticationRequest createFromOtpCode(String otpCode) {
        return new OtpChallengeAuthenticationRequest(otpCode);
    }

    public String getOtpCode() {
        return otpCode;
    }
}
