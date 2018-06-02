package se.tink.backend.auth;

import java.util.Optional;
import se.tink.backend.core.Client;
import se.tink.backend.core.oauth2.OAuth2Client;

public class OAuth2ClientRequest {

    private Optional<Client> linkClient;
    private Optional<OAuth2Client> oAuth2Client;

    private OAuth2ClientRequest(Client linkClient, OAuth2Client oAuth2Client) {
        this.linkClient = Optional.ofNullable(linkClient);
        this.oAuth2Client = Optional.ofNullable(oAuth2Client);
    }

    public Optional<Client> getLinkClient() {
        return linkClient;
    }

    public Optional<OAuth2Client> getoAuth2Client() {
        return oAuth2Client;
    }

    public static class Builder {

        private Client linkClient;
        private OAuth2Client oAuth2Client;

        public Builder setLinkClient(Client linkClient) {
            this.linkClient = linkClient;
            return this;
        }

        public Builder setOAuth2Client(OAuth2Client oAuth2Client) {
            this.oAuth2Client = oAuth2Client;
            return this;
        }

        public OAuth2ClientRequest build() {
            return new OAuth2ClientRequest(linkClient, oAuth2Client);
        }
    }
}
