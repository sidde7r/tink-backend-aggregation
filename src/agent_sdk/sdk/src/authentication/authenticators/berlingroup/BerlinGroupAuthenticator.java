package se.tink.agent.sdk.authentication.authenticators.berlingroup;

public interface BerlinGroupAuthenticator
        extends BerlinGroupGetConfiguration, BerlinGroupCreateConsent, BerlinGroupGetConsentStatus {

    String STATE_KEY_CONSENT_ID = "berlingroup_consent_id";
}
