package se.tink.agent.sdk.authentication.authenticators.oauth2_decoupled_app;

import se.tink.agent.sdk.authentication.authenticators.oauth2.RefreshableAccessTokenAndConsentLifetime;

public interface FetchAccessToken {
    RefreshableAccessTokenAndConsentLifetime fetchAccessToken();
}
