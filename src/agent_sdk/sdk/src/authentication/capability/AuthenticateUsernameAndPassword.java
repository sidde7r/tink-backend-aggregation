package se.tink.agent.sdk.authentication.capability;

import se.tink.agent.sdk.authentication.authenticators.username_password.UsernameAndPasswordAuthenticator;

public interface AuthenticateUsernameAndPassword {
    UsernameAndPasswordAuthenticator authenticator();
}
