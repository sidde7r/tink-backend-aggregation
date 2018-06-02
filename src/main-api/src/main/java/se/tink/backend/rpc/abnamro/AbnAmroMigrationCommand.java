package se.tink.backend.rpc.abnamro;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.core.User;
import se.tink.libraries.validation.exceptions.InvalidPin6Exception;
import se.tink.libraries.validation.validators.Pin6Validator;

public class AbnAmroMigrationCommand {
    private String smsOtpVerificationToken;
    private String pin6;
    private Optional<String> remoteAddress;
    private User user;
    private String userDeviceId;
    private String userAgent;

    private AbnAmroMigrationCommand() {
    }

    public User getUser() {
        return user;
    }

    public String getSmsOtpVerificationToken() {
        return smsOtpVerificationToken;
    }

    public String getPin6() {
        return pin6;
    }

    public Optional<String> getRemoteAddress() {
        return remoteAddress;
    }

    public String getUserDeviceId() {
        return userDeviceId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getUserAgent() {
        return userAgent;
    }
    
    public final static class Builder {
        private String smsOtpVerificationToken;
        private String pin6;
        private User user;
        private Optional<String> remoteAddress;
        private String userDeviceId;
        private String userAgent;

        public Builder withSmsOtpVerificationToken(String smsOtpVerificationToken) {
            this.smsOtpVerificationToken = smsOtpVerificationToken;
            return this;
        }

        public Builder withPin6(String pin6) {
            this.pin6 = pin6;
            return this;
        }

        public Builder withRemoteAddress(Optional<String> remoteAddress) {
            this.remoteAddress = remoteAddress;
            return this;
        }

        public Builder withUser(User user) {
            this.user = user;
            return this;
        }

        public Builder withUserDeviceId(String userDeviceId) {
            this.userDeviceId = userDeviceId;
            return this;
        }

        public Builder withUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public AbnAmroMigrationCommand build() throws InvalidPin6Exception {
            Preconditions.checkNotNull(user, "User must not be null.");
            Preconditions.checkState(!Strings.isNullOrEmpty(smsOtpVerificationToken),
                    "Sms otp verification token must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(userDeviceId), "UserDeviceId must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(userAgent), "UserAgent must not be null or empty.");

            Pin6Validator.validate(pin6);

            AbnAmroMigrationCommand command = new AbnAmroMigrationCommand();
            command.smsOtpVerificationToken = smsOtpVerificationToken;
            command.pin6 = pin6;
            command.remoteAddress = remoteAddress;
            command.user = user;
            command.userDeviceId = userDeviceId;
            command.userAgent = userAgent;

            return command;
        }
    }
}
