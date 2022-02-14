package se.tink.agent.compatibility_layers.aggregation_service.authentication.report;

import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.consent.ConsentStatus;

public class AuthenticationReport {

    @Nullable private final ConsentLifetime consentLifetime;
    private final ConsentStatus consentStatus;

    public AuthenticationReport(
            ConsentStatus consentStatus, @Nullable ConsentLifetime consentLifetime) {
        this.consentLifetime = consentLifetime;
        this.consentStatus = consentStatus;
    }

    public Optional<ConsentLifetime> getConsentLifetime() {
        return Optional.ofNullable(consentLifetime);
    }

    public ConsentStatus getConsentStatus() {
        return consentStatus;
    }
}
