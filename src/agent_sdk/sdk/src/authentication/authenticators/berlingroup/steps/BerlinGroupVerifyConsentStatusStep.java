package se.tink.agent.sdk.authentication.authenticators.berlingroup.steps;

import java.util.Optional;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupAuthenticatorConfiguration;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupGetConfiguration;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupGetConsentStatus;
import se.tink.agent.sdk.authentication.existing_consent.ConsentStatus;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentRequest;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentResponse;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentStep;

public class BerlinGroupVerifyConsentStatusStep implements ExistingConsentStep {
    private final BerlinGroupGetConfiguration agentGetConfiguration;
    private final BerlinGroupGetConsentStatus agentGetConsentStatus;

    public BerlinGroupVerifyConsentStatusStep(
            BerlinGroupGetConfiguration agentGetConfiguration,
            BerlinGroupGetConsentStatus agentGetConsentStatus) {
        this.agentGetConfiguration = agentGetConfiguration;
        this.agentGetConsentStatus = agentGetConsentStatus;
    }

    @Override
    public ExistingConsentResponse execute(ExistingConsentRequest request) {
        // Read the `consentId` from the AgentStorage, previously written by
        // {@link #BerlinGroupVerifyAuthorizedConsentStep}
        BerlinGroupAuthenticatorConfiguration configuration =
                this.agentGetConfiguration.getConfiguration();
        Optional<String> maybeConsentId =
                request.getAgentStorage().tryGet(configuration.getConsentIdStorageKey());

        ConsentStatus consentStatus =
                maybeConsentId
                        .map(this.agentGetConsentStatus::getConsentStatus)
                        .orElse(ConsentStatus.EXPIRED);

        return ExistingConsentResponse.done(consentStatus);
    }
}
