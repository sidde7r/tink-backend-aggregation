package se.tink.agent.sdk.authentication.authenticators.berlingroup;

import se.tink.agent.sdk.authentication.consent.ConsentStatus;

public interface BerlinGroupGetConsentStatus {
    ConsentStatus getConsentStatus(String consentId);
}
