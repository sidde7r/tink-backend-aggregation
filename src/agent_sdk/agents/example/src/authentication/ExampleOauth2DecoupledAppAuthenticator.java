package se.tink.agent.agents.example.authentication;

import se.tink.agent.sdk.authentication.authenticators.oauth2.AccessTokenAndConsentLifetime;
import se.tink.agent.sdk.authentication.authenticators.oauth2_decoupled_app.Oauth2DecoupledAppAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.result.ThirdPartyAppResult;
import se.tink.agent.sdk.authentication.consent.ConsentStatus;
import se.tink.agent.sdk.storage.Reference;
import se.tink.agent.sdk.user_interaction.ThirdPartyAppInfo;
import se.tink.agent.sdk.user_interaction.UserInteraction;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class ExampleOauth2DecoupledAppAuthenticator implements Oauth2DecoupledAppAuthenticator {
    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) {
        return null;
    }

    @Override
    public ConsentStatus verifyBankConnection() {
        return null;
    }

    @Override
    public ThirdPartyAppResult initThirdPartyAppAuthentication() {
        return null;
    }

    @Override
    public UserInteraction<ThirdPartyAppInfo> getThirdPartyAppInfo(Reference reference) {
        return null;
    }

    @Override
    public ThirdPartyAppResult pollThirdPartyAppStatus(Reference reference) {
        return null;
    }

    @Override
    public AccessTokenAndConsentLifetime fetchAccessToken() {
        return null;
    }
}
