package se.tink.backend.sms.otp.rpc;

import java.util.Objects;
import java.util.UUID;
import se.tink.backend.sms.otp.core.SmsOtpVerificationResult;

public class VerifySmsOtpResponse {
    private SmsOtpVerificationResult result;
    private String phoneNumber;
    private UUID otpId;

    public SmsOtpVerificationResult getResult() {
        return result;
    }

    public void setResult(SmsOtpVerificationResult result) {
        this.result = result;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setOtpId(UUID otpId) {
        this.otpId = otpId;
    }

    public UUID getOtpId() {
        return otpId;
    }

    public boolean isCorrectCode() {
        return Objects.equals(SmsOtpVerificationResult.CORRECT_CODE, result);
    }
}
