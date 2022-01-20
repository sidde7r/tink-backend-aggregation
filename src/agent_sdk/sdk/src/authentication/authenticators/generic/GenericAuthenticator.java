package se.tink.agent.sdk.authentication.authenticators.generic;

import se.tink.agent.sdk.authentication.steppable_execution.ExistingConsentFlow;
import se.tink.agent.sdk.authentication.steppable_execution.NewConsentFlow;

public interface GenericAuthenticator {
    // "full authentication"
    NewConsentFlow issueNewConsent();

    // "is logged in"
    ExistingConsentFlow useExistingConsent();
}
