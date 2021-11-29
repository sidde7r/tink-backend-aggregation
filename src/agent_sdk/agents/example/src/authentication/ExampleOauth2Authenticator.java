package se.tink.agent.agents.example.authentication;

import java.net.URI;
import se.tink.agent.sdk.authentication.authenticators.oauth2.Oauth2Authenticator;
import se.tink.agent.sdk.authentication.authenticators.oauth2.RefreshableAccessTokenAndConsentLifetime;
import se.tink.agent.sdk.authentication.existing_consent.ConsentStatus;
import se.tink.agent.sdk.models.authentication.RefreshableAccessToken;
import se.tink.agent.sdk.user_interaction.UserResponseData;

public class ExampleOauth2Authenticator implements Oauth2Authenticator {

    @Override
    public URI buildAuthorizationAppUrl(String state) {
        return null;
    }

    @Override
    public RefreshableAccessTokenAndConsentLifetime exchangeAuthorizationCode(
            String authorizationCode) {
        return null;
    }

    @Override
    public RefreshableAccessToken refreshAccessToken(String refreshToken) {
        return null;
    }

    @Override
    public void handleCallbackDataError(UserResponseData callbackData) {}

    @Override
    public ConsentStatus verifyBankConnection() {
        return null;
    }
}
