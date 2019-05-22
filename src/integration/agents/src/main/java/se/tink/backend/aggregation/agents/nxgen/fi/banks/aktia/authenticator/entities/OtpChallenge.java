package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OtpChallenge {

    @JsonProperty("otpRequired")
    private boolean otpRequired;

    @JsonProperty("otpInfo")
    private OtpInfo otpInfo;

    @JsonProperty("readMessagesWithoutOtp")
    private boolean readMessagesWithoutOtp;

    public boolean isOtpRequired() {
        return otpRequired;
    }

    public OtpInfo getOtpInfo() {
        return otpInfo;
    }

    public boolean isReadMessagesWithoutOtp() {
        return readMessagesWithoutOtp;
    }
}
