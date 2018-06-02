package se.tink.backend.rpc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;

public class InitiateBankIdAuthenticationCommand {
    private String nationalId;
    private String market;
    private String clientId;
    private String oauth2ClientId;
    private String deviceId;
    private String authenticationToken;

    public Optional<String> getNationalId() {
        return Optional.ofNullable(nationalId);
    }

    public Optional<String> getMarket() {
        return Optional.ofNullable(market);
    }

    public Optional<String> getClientId() {
        return Optional.ofNullable(clientId);
    }

    public Optional<String> getOauth2ClientId() {
        return Optional.ofNullable(oauth2ClientId);
    }

    public Optional<String> getDeviceId() {
        return Optional.ofNullable(deviceId);
    }

    public Optional<String> getAuthenticationToken() {
        return Optional.ofNullable(authenticationToken);
    }

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {
        private String nationalId;
        private String market;
        private String clientId;
        private String oauth2ClientId;
        private String deviceId;
        private String authenticationToken;

        public Builder withNationalId(String nationalId) {
            this.nationalId = Strings.emptyToNull(nationalId);
            return this;
        }

        public Builder withMarket(String market) {
            this.market = market;
            return this;
        }

        public Builder withClient(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder withOauth2ClientId(String oauth2ClientId) {
            this.oauth2ClientId = oauth2ClientId;
            return this;
        }

        public Builder withDeviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder withAuthenticationToken(String authenticationToken) {
            this.authenticationToken = Strings.emptyToNull(authenticationToken);
            return this;
        }

        public InitiateBankIdAuthenticationCommand build() {
            Preconditions.checkState(Strings.isNullOrEmpty(authenticationToken) || Strings.isNullOrEmpty(nationalId),
                            "Both authentication token and nationalId cannot be used at the same time");

            InitiateBankIdAuthenticationCommand command = new InitiateBankIdAuthenticationCommand();
            command.nationalId = nationalId;
            command.market = market;
            command.oauth2ClientId = oauth2ClientId;
            command.deviceId = deviceId;
            command.clientId = clientId;
            command.authenticationToken = authenticationToken;

            return command;
        }
    }
}
