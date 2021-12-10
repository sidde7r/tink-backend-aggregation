package se.tink.agent.agents.example.authentication;

import java.time.LocalDate;
import java.time.Period;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupAuthenticatorConfiguration;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupConsent;
import se.tink.agent.sdk.authentication.existing_consent.ConsentStatus;

public class ExampleBerlinGroupAuthenticator implements BerlinGroupAuthenticator {
    @Override
    public BerlinGroupAuthenticatorConfiguration getConfiguration() {
        return BerlinGroupAuthenticatorConfiguration.builder()
                .consentIdStorageKey("CONSENT_ID")
                .consentValidForPeriod(Period.ofDays(89))
                .build();
    }

    @Override
    public BerlinGroupConsent createConsent(String state, LocalDate consentValidUntil) {
        return null;
    }

    @Override
    public ConsentStatus getConsentStatus(String consentId) {
        return null;
    }
}
