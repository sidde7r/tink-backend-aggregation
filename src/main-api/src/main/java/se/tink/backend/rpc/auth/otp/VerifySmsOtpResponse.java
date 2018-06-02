package se.tink.backend.rpc.auth.otp;

import se.tink.backend.sms.otp.core.SmsOtpVerificationResult;

public class VerifySmsOtpResponse {
    private SmsOtpVerificationResult result;
    private String token;
    private boolean existingUser;

    public void setResult(SmsOtpVerificationResult result) {
        this.result = result;
    }

    public SmsOtpVerificationResult getResult() {
        return result;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public boolean isExistingUser() {
        return existingUser;
    }

    public void setExistingUser(boolean existingUser) {
        this.existingUser = existingUser;
    }
}
