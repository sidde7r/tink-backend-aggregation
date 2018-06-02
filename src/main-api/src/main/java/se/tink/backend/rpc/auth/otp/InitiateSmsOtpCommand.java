package se.tink.backend.rpc.auth.otp;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.core.User;
import se.tink.libraries.phonenumbers.InvalidPhoneNumberException;
import se.tink.libraries.phonenumbers.utils.PhoneNumberUtils;
import se.tink.libraries.validation.exceptions.InvalidLocaleException;
import se.tink.libraries.validation.validators.LocaleValidator;

public class InitiateSmsOtpCommand {
    private String phoneNumber;
    private String locale;
    private Optional<User> user;
    private Optional<String> remoteAddress;

    private InitiateSmsOtpCommand() {
    }

    public Optional<User> getUser() {
        return user;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLocale() {
        return locale;
    }

    public Optional<String> getRemoteAddress() {
        return remoteAddress;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {
        private String phoneNumber;
        private String locale;
        private Optional<User> user = Optional.empty();
        private Optional<String> remoteAddress = Optional.empty();

        public Builder withPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder withLocale(String locale) {
            this.locale = locale;
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

        public InitiateSmsOtpCommand build() throws InvalidPhoneNumberException, InvalidLocaleException {
            Preconditions.checkState(!Strings.isNullOrEmpty(phoneNumber), "Phone number must not be null or empty.");

            // Use local from user if locale isn't provided
            if (Strings.isNullOrEmpty(locale) && user.isPresent()) {
                locale = user.get().getLocale();
            }

            LocaleValidator.validate(locale);

            InitiateSmsOtpCommand command = new InitiateSmsOtpCommand();
            command.phoneNumber = PhoneNumberUtils.normalize(phoneNumber);
            command.remoteAddress = remoteAddress;
            command.user = user;
            command.locale = locale;

            return command;
        }
    }
}

