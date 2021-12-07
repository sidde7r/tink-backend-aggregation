package se.tink.agent.sdk.authentication.features;

import se.tink.agent.sdk.authentication.authenticators.username_password.UsernameAndPasswordAuthenticator;

public interface AuthenticateUsernameAndPassword {
    UsernameAndPasswordAuthenticator authenticator();
}
