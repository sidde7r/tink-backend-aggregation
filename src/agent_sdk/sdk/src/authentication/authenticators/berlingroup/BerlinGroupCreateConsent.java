package se.tink.agent.sdk.authentication.authenticators.berlingroup;

import java.time.LocalDate;

public interface BerlinGroupCreateConsent {
    BerlinGroupConsent createConsent(String state, LocalDate consentValidUntil);
}
