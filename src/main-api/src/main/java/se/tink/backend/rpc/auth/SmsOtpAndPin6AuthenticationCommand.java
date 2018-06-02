package se.tink.backend.rpc.auth;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.libraries.validation.exceptions.InvalidPin6Exception;

public class SmsOtpAndPin6AuthenticationCommand {
    private String smsOtpVerificationToken;
    private String pin6;
    private String clientKey;
    private String oauthClientId;
    private String market;
    private String deviceId;
    private String remoteAddress;

    public SmsOtpAndPin6AuthenticationCommand(String smsOtpVerificationToken, String pin6, String clientKey,
            String oauthClientId, String market, String deviceId, String remoteAddress) throws InvalidPin6Exception {
        Preconditions.checkState(!Strings.isNullOrEmpty(smsOtpVerificationToken),
                "Sms otp verification token must not be null or empty.");
        Preconditions.checkState(!Strings.isNullOrEmpty(pin6), "Pin6 may not be null or empty.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId));

        this.smsOtpVerificationToken = smsOtpVerificationToken;
        this.pin6 = pin6;
        this.clientKey = clientKey;
        this.oauthClientId = oauthClientId;
        this.market = market;
        this.deviceId = deviceId;
        this.remoteAddress = remoteAddress;
    }

    public String getSmsOtpVerificationToken() {
        return smsOtpVerificationToken;
    }

    public String getPin6() {
        return pin6;
    }

    public String getOauthClientId() {
        return oauthClientId;
    }

    public String getClientKey() {
        return clientKey;
    }

    public String getMarket() {
        return market;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }
}
