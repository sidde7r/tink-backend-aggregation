package se.tink.backend.rpc.auth;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.core.User;
import se.tink.libraries.validation.exceptions.InvalidPin6Exception;
import se.tink.libraries.validation.validators.Pin6Validator;

public class UpdatePhoneNumberCommand {
    private String smsOtpVerificationToken;
    private User user;
    private String pin6;
    private Optional<String> remoteAddress;
    private Optional<String> sessionId;

    private UpdatePhoneNumberCommand() {
    }

    public String getSmsOtpVerificationToken() {
        return smsOtpVerificationToken;
    }

    public Optional<String> getRemoteAddress() {
        return remoteAddress;
    }

    public User getUser() {
        return user;
    }

    public String getPin6() {
        return pin6;
    }

    public Optional<String> getSessionId() {
        return sessionId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {
        private String smsOtpVerificationToken;
        private String pin6;
        private User user;
        private Optional<String> sessionId;
        private Optional<String> remoteAddress;

        public Builder withSmsOtpVerificationToken(String smsOtpVerificationToken) {
            this.smsOtpVerificationToken = smsOtpVerificationToken;
            return this;
        }

        public Builder withPin6(String pin6) {
            this.pin6 = pin6;
            return this;
        }

        public Builder withUser(User user) {
            this.user = user;
            return this;
        }

        public Builder withSessionId(Optional<String> sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder withRemoteAddress(Optional<String> remoteAddress) {
            this.remoteAddress = remoteAddress;
            return this;
        }

        public UpdatePhoneNumberCommand build() throws InvalidPin6Exception {
            Pin6Validator.validate(pin6);
            Preconditions.checkNotNull(user);
            Preconditions.checkState(!Strings.isNullOrEmpty(smsOtpVerificationToken),
                    "Sms otp verification token must not be null or empty.");

            UpdatePhoneNumberCommand command = new UpdatePhoneNumberCommand();
            command.pin6 = pin6;
            command.smsOtpVerificationToken = smsOtpVerificationToken;
            command.sessionId = sessionId;
            command.remoteAddress = remoteAddress;
            command.user = user;

            return command;
        }

    }
}
