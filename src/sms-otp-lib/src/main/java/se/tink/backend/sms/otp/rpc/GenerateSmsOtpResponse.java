package se.tink.backend.sms.otp.rpc;

import java.util.Date;
import java.util.UUID;

public class GenerateSmsOtpResponse {
    private UUID id;
    private Date expireAt;
    private int otpLength;

    public UUID getId() {
        return id;
    }

    public int getOtpLength() {
        return otpLength;
    }

    public void setOtpLength(int otpLength) {
        this.otpLength = otpLength;
    }

    public Date getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Date expireAt) {
        this.expireAt = expireAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
