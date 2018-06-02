package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignOTPRequest {
    private String transId;
    private String otp;
    private String pin;

    public SignOTPRequest setTransId(String transId) {
        this.transId = transId;
        return this;
    }

    public SignOTPRequest setOtp(String otp) {
        this.otp = otp;
        return this;
    }

    public SignOTPRequest setPin(String pin) {
        this.pin = pin;
        return this;
    }
}
