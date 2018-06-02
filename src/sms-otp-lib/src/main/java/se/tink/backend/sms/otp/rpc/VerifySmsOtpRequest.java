package se.tink.backend.sms.otp.rpc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.UUID;

public class VerifySmsOtpRequest {
    private UUID otpId;
    private String code;

    public VerifySmsOtpRequest(String otpId, String code) {
        Preconditions.checkState(!Strings.isNullOrEmpty(otpId), "Otp Id is null or empty");
        Preconditions.checkState(!Strings.isNullOrEmpty(code), "Code is null or empty");
        this.otpId = UUID.fromString(otpId);
        this.code = code;
    }

    public UUID getOtpId() {
        return otpId;
    }

    public String getCode() {
        return code;
    }
}
