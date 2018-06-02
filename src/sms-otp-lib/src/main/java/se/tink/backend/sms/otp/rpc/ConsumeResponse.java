package se.tink.backend.sms.otp.rpc;

import se.tink.backend.sms.otp.core.SmsOtpConsumeResult;

public class ConsumeResponse {
    private String phoneNumber;
    private SmsOtpConsumeResult result;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public SmsOtpConsumeResult getResult() {
        return result;
    }

    public void setResult(SmsOtpConsumeResult result) {
        this.result = result;
    }
}
