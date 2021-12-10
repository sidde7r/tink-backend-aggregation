package se.tink.agent.sdk.authentication.authenticators.berlingroup;

import se.tink.agent.sdk.authentication.existing_consent.ConsentStatus;

public interface BerlinGroupGetConsentStatus {
    ConsentStatus getConsentStatus(String consentId);
}
