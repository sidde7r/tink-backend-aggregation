package se.tink.agent.sdk.authentication.authenticators.username_password;

import se.tink.agent.sdk.authentication.consent.ConsentLifetime;

public interface UsernameAndPasswordLogin {
    ConsentLifetime login(String username, String password);
}
