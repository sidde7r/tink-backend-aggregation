package se.tink.agent.sdk.authentication.existing_consent;

public interface ExistingConsentStep {
    ExistingConsentResponse execute(final ExistingConsentRequest request);
}
