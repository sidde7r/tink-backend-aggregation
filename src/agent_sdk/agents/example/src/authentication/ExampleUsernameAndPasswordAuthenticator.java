package se.tink.agent.agents.example.authentication;

import se.tink.agent.sdk.authentication.authenticators.username_password.UsernameAndPasswordAuthenticator;
import se.tink.agent.sdk.authentication.existing_consent.ConsentStatus;
import se.tink.agent.sdk.authentication.new_consent.ConsentLifetime;

public class ExampleUsernameAndPasswordAuthenticator implements UsernameAndPasswordAuthenticator {
    @Override
    public ConsentLifetime login(String username, String password) {
        return null;
    }

    @Override
    public ConsentStatus verifyBankConnection() {
        return null;
    }
}
