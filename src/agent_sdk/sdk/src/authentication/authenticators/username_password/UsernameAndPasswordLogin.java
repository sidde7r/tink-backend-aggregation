package se.tink.agent.sdk.authentication.authenticators.username_password;

import se.tink.agent.sdk.authentication.new_consent.ConsentLifetime;

public interface UsernameAndPasswordLogin {
    ConsentLifetime login(String username, String password);
}
