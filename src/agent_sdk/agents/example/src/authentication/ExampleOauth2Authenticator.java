package se.tink.agent.agents.example.authentication;

import se.tink.agent.sdk.authentication.authenticators.oauth2.AccessTokenAndConsentLifetime;
import se.tink.agent.sdk.authentication.authenticators.oauth2.Oauth2Authenticator;
import se.tink.agent.sdk.authentication.existing_consent.ConsentStatus;
import se.tink.agent.sdk.user_interaction.UserResponseData;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class ExampleOauth2Authenticator implements Oauth2Authenticator {

    @Override
    public URL buildAuthorizationAppUrl(String state) {
        return null;
    }

    @Override
    public AccessTokenAndConsentLifetime exchangeAuthorizationCode(String authorizationCode) {
        return null;
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) {
        return null;
    }

    @Override
    public void handleCallbackDataError(UserResponseData callbackData) {}

    @Override
    public ConsentStatus verifyBankConnection() {
        return null;
    }
}
