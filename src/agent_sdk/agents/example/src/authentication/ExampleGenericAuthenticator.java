package se.tink.agent.agents.example.authentication;

import se.tink.agent.sdk.authentication.authenticators.generic.AuthenticationFlow;
import se.tink.agent.sdk.authentication.authenticators.generic.GenericAuthenticator;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentStep;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;

public class ExampleGenericAuthenticator implements GenericAuthenticator {
    @Override
    public AuthenticationFlow<NewConsentStep> issueNewConsent() {
        return null;
    }

    @Override
    public AuthenticationFlow<ExistingConsentStep> useExistingConsent() {
        return null;
    }
}
