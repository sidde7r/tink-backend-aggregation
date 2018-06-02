package se.tink.backend.rpc.auth.otp;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.core.User;

public class VerifySmsOtpCommand {
    private String smsOtpVerificationToken;
    private String code;
    private Optional<User> user;
    private Optional<String> remoteAddress;

    private VerifySmsOtpCommand() {
    }

    public String getSmsOtpVerificationToken() {
        return smsOtpVerificationToken;
    }

    public String getCode() {
        return code;
    }

    public Optional<User> getUser() {
        return user;
    }

    public Optional<String> getRemoteAddress() {
        return remoteAddress;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {
        private String smsOtpVerificationToken;
        private String code;
        private Optional<User> user = Optional.empty();
        private Optional<String> remoteAddress = Optional.empty();

        public Builder withSmsOtpVerificationToken(String smsOtpVerificationToken) {
            this.smsOtpVerificationToken = smsOtpVerificationToken;
            return this;
        }

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withRemoteAddress(Optional<String> remoteAddress) {
            this.remoteAddress = remoteAddress;
            return this;
        }

        public Builder withUser(Optional<User> user) {
            this.user = user;
            return this;
        }

        public VerifySmsOtpCommand build() {
            Preconditions.checkState(!Strings.isNullOrEmpty(smsOtpVerificationToken),
                    "Sms otp verification token must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(code), "Code must not be null or empty.");

            VerifySmsOtpCommand command = new VerifySmsOtpCommand();
            command.smsOtpVerificationToken = smsOtpVerificationToken;
            command.remoteAddress = remoteAddress;
            command.user = user;
            command.code = code;

            return command;
        }
    }
}
