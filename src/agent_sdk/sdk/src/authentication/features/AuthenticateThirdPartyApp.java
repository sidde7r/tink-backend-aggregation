package se.tink.agent.sdk.authentication.features;

import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppAuthenticator;

public interface AuthenticateThirdPartyApp {
    ThirdPartyAppAuthenticator authenticator();
}
