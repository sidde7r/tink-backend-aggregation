package se.tink.backend.rpc.auth;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.libraries.validation.exceptions.InvalidPin6Exception;
import se.tink.libraries.validation.validators.Pin6Validator;

public class ResetPin6Command {
    private String smsOtpVerificationToken;
    private String pin6;
    private String clientKey;
    private String oauthClientId;
    private String remoteAddress;
    private String userAgent;
    private String userDeviceId;

    private ResetPin6Command() {
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

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getUserDeviceId() {
        return userDeviceId;
    }

    public static ResetPin6Command.Builder builder() {
        return new ResetPin6Command.Builder();
    }

    public final static class Builder {
        private String smsOtpVerificationToken;
        private String pin6;
        private String clientKey;
        private String oauthClientId;
        private String remoteAddress;
        private String userAgent;
        private String userDeviceId;

        public Builder withSmsOtpVerificationToken(String smsOtpVerificationToken) {
            this.smsOtpVerificationToken = smsOtpVerificationToken;
            return this;
        }

        public Builder withPin6(String pin6) {
            this.pin6 = pin6;
            return this;
        }

        public Builder withClientKey(String clientKey) {
            this.clientKey = clientKey;
            return this;
        }

        public Builder withOauthClientId(String oauthClientId) {
            this.oauthClientId = oauthClientId;
            return this;
        }

        public Builder withRemoteAddress(String remoteAddress) {
            this.remoteAddress = remoteAddress;
            return this;
        }

        public Builder withUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder withUserDeviceId(String userDeviceId) {
            this.userDeviceId = userDeviceId;
            return this;
        }

        public ResetPin6Command build() throws InvalidPin6Exception {
            Pin6Validator.validate(pin6);
            Preconditions.checkState(!Strings.isNullOrEmpty(smsOtpVerificationToken),
                    "Sms otp verification token must not be null or empty.");

            Preconditions.checkState(!Strings.isNullOrEmpty(userDeviceId), "DeviceId must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(remoteAddress), "RemoteAddress must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(userAgent), "UserAgent must not be null or empty.");

            ResetPin6Command command = new ResetPin6Command();
            command.pin6 = pin6;
            command.smsOtpVerificationToken = smsOtpVerificationToken;
            command.remoteAddress = remoteAddress;
            command.clientKey = clientKey;
            command.oauthClientId = oauthClientId;
            command.remoteAddress = remoteAddress;
            command.userAgent = userAgent;
            command.userDeviceId = userDeviceId;

            return command;
        }
    }
}
