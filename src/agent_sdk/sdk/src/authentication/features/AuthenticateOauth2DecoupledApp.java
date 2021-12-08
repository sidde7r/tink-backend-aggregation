package se.tink.agent.sdk.authentication.features;

import se.tink.agent.sdk.authentication.authenticators.oauth2_decoupled_app.Oauth2DecoupledAppAuthenticator;

public interface AuthenticateOauth2DecoupledApp {
    Oauth2DecoupledAppAuthenticator authenticator();
}
