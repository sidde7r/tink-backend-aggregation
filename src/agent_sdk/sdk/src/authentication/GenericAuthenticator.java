package se.tink.agent.sdk.authentication;

import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentStep;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;

public interface GenericAuthenticator {
    // "full authentication"
    AuthenticationFlow<NewConsentStep> issueNewConsent();

    // "is logged in"
    AuthenticationFlow<ExistingConsentStep> useExistingConsent();
}
