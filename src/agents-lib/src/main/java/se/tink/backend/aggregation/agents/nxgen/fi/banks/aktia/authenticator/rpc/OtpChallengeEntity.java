package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OtpChallengeEntity {
    private boolean otpRequired;
    private OtpInfoEntity otpInfo;
    private boolean readMessagesWithoutOtp;

    public boolean isOtpRequired() {
        return otpRequired;
    }

    public OtpInfoEntity getOtpInfo() {
        return otpInfo;
    }

    public boolean isReadMessagesWithoutOtp() {
        return readMessagesWithoutOtp;
    }
}
