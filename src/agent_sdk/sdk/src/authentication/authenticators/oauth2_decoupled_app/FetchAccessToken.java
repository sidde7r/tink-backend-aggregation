package se.tink.agent.sdk.authentication.authenticators.oauth2_decoupled_app;

import se.tink.agent.sdk.authentication.authenticators.oauth2.AccessTokenAndConsentLifetime;

public interface FetchAccessToken {
    AccessTokenAndConsentLifetime fetchAccessToken();
}
