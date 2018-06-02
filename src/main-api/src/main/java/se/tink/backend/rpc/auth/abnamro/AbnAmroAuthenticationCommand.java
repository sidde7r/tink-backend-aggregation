package se.tink.backend.rpc.auth.abnamro;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class AbnAmroAuthenticationCommand {
    private String internetBankingSessionToken;
    private String clientKey;
    private String oauthClientId;

    private AbnAmroAuthenticationCommand() {
    }

    public String getInternetBankingSessionToken() {
        return internetBankingSessionToken;
    }

    public String getClientKey() {
        return clientKey;
    }

    public String getOauthClientId() {
        return oauthClientId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {
        private String clientKey;
        private String oauthClientId;
        private String internetBankingSessionToken;

        public Builder withInternetBankingSessionToken(String internetBankingSessionToken) {
            this.internetBankingSessionToken = internetBankingSessionToken;
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

        public AbnAmroAuthenticationCommand build() {
            Preconditions.checkState(!Strings.isNullOrEmpty(internetBankingSessionToken),
                    "Internet Banking Session Token must not be null or empty");

            AbnAmroAuthenticationCommand command = new AbnAmroAuthenticationCommand();
            command.clientKey = clientKey;
            command.oauthClientId = oauthClientId;
            command.internetBankingSessionToken = internetBankingSessionToken;

            return command;
        }
    }
}
