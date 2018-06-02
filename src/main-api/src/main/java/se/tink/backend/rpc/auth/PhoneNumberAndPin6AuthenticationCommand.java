package se.tink.backend.rpc.auth;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.libraries.phonenumbers.InvalidPhoneNumberException;
import se.tink.libraries.phonenumbers.utils.PhoneNumberUtils;

public class PhoneNumberAndPin6AuthenticationCommand {
    private String phoneNumber;
    private String pin6;
    private String clientKey;
    private String oauthClientId;
    private String market;
    private Optional<String> remoteAddress;
    private String userAgent;
    private String userDeviceId;

    private PhoneNumberAndPin6AuthenticationCommand() {
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPin6() {
        return pin6;
    }

    public String getClientKey() {
        return clientKey;
    }

    public String getOauthClientId() {
        return oauthClientId;
    }

    public String getMarket() {
        return market;
    }

    public Optional<String> getRemoteAddress() {
        return remoteAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getUserDeviceId() {
        return userDeviceId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {
        private String phoneNumber;
        private String pin6;
        private String clientKey;
        private String oauthClientId;
        private String market;
        private Optional<String> remoteAddress;
        private String userAgent;
        private String userDeviceId;

        public Builder withPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder withPin6(String pin6) {
            this.pin6 = pin6;
            return this;
        }

        public Builder withMarket(String market) {
            this.market = market;
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

        public Builder withRemoteAddress(Optional<String> remoteAddress) {
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

        public PhoneNumberAndPin6AuthenticationCommand build() throws InvalidPhoneNumberException {
            Preconditions.checkState(!Strings.isNullOrEmpty(phoneNumber), "Phone number must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(pin6), "Pin6 may not be null or empty.");

            Preconditions.checkState(!Strings.isNullOrEmpty(userAgent), "UserAgent must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(userDeviceId), "UserDeviceId may not be null or empty.");

            PhoneNumberAndPin6AuthenticationCommand command = new PhoneNumberAndPin6AuthenticationCommand();
            command.phoneNumber = PhoneNumberUtils.normalize(phoneNumber);
            command.pin6 = pin6;
            command.clientKey = clientKey;
            command.oauthClientId = oauthClientId;
            command.market = market;
            command.remoteAddress = remoteAddress;
            command.userAgent = userAgent;
            command.userDeviceId = userDeviceId;

            return command;
        }
    }
}
