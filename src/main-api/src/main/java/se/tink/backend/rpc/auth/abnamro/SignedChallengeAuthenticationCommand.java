package se.tink.backend.rpc.auth.abnamro;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;

public class SignedChallengeAuthenticationCommand {
    private String token;
    private String clientKey;
    private String oauthClientId;
    private Optional<String> remoteAddress;
    private String userAgent;
    private String userDeviceId;
    private String market;

    private SignedChallengeAuthenticationCommand() {
    }

    public String getToken() {
        return token;
    }

    public String getClientKey() {
        return clientKey;
    }

    public String getOauthClientId() {
        return oauthClientId;
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

    public String getMarket() {
        return market;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {
        private String token;
        private String clientKey;
        private String oauthClientId;
        private Optional<String> remoteAddress;
        private String userAgent;
        private String userDeviceId;
        private String market;

        public Builder withToken(String token) {
            this.token = token;
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

        public Builder withMarket(String market) {
            this.market = market;
            return this;
        }

        public SignedChallengeAuthenticationCommand build() {
            Preconditions.checkState(!Strings.isNullOrEmpty(token), "Token may not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(userAgent), "UserAgent must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(userDeviceId), "UserDeviceId may not be null or empty.");

            SignedChallengeAuthenticationCommand command = new SignedChallengeAuthenticationCommand();
            command.token = token;
            command.clientKey = clientKey;
            command.oauthClientId = oauthClientId;
            command.remoteAddress = remoteAddress;
            command.userAgent = userAgent;
            command.userDeviceId = userDeviceId;
            command.market = market;

            return command;
        }
    }
}
