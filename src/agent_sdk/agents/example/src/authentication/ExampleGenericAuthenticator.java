package se.tink.agent.agents.example.authentication;

import se.tink.agent.sdk.authentication.authenticators.generic.GenericAuthenticator;
import se.tink.agent.sdk.authentication.steppable_execution.ExistingConsentFlow;
import se.tink.agent.sdk.authentication.steppable_execution.NewConsentFlow;

public class ExampleGenericAuthenticator implements GenericAuthenticator {
    @Override
    public NewConsentFlow issueNewConsent() {
        return null;
    }

    @Override
    public ExistingConsentFlow useExistingConsent() {
        return null;
    }
}
