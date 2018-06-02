package se.tink.backend.rpc.auth.otp;

import java.util.Date;

public class InitiateSmsOtpResponse {
    private String token;
    private Date expireAt;
    private int otpLength;

    public String getToken() {
        return token;
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

    public void setToken(String token) {
        this.token = token;
    }
}
