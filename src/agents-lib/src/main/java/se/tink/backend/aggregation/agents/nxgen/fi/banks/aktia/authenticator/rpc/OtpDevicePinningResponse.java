package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OtpDevicePinningResponse {
    private String activationCode;
    private String userId;
    private ResultEntity result;
    private OtpResponseEntity otpResponse;

    public String getActivationCode() {
        return activationCode;
    }

    public String getUserId() {
        return userId;
    }

    public ResultEntity getResult() {
        return result;
    }

    public OtpResponseEntity getOtpResponse() {
        return otpResponse;
    }

    public Optional<String> getOtpIndex() {
        OtpResponseEntity otpResponse = getOtpResponse();
        if (otpResponse == null) {
            return Optional.empty();
        }

        OtpInfoEntity otpInfo = otpResponse.getOtpInfo();
        if (otpInfo == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(otpInfo.getNextOtpIndex());
    }
}
