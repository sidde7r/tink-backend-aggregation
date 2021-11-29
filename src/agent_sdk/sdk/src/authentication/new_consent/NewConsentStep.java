package se.tink.agent.sdk.authentication.new_consent;

import se.tink.agent.sdk.authentication.new_consent.response.NewConsentResponse;

public interface NewConsentStep {
    NewConsentResponse execute(final NewConsentRequest request);
}
