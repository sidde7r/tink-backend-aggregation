package se.tink.agent.sdk.authentication.authenticators.berlingroup.steps;

import java.util.Optional;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupAuthenticatorConfiguration;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupGetConfiguration;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupGetConsentStatus;
import se.tink.agent.sdk.authentication.consent.ConsentStatus;
import se.tink.agent.sdk.authentication.steppable_execution.ExistingConsentStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequestBase;
import se.tink.agent.sdk.steppable_execution.non_interactive_step.NonInteractionStepResponse;

public class BerlinGroupVerifyConsentStatusStep extends ExistingConsentStep {
    private final BerlinGroupGetConfiguration agentGetConfiguration;
    private final BerlinGroupGetConsentStatus agentGetConsentStatus;

    public BerlinGroupVerifyConsentStatusStep(
            BerlinGroupGetConfiguration agentGetConfiguration,
            BerlinGroupGetConsentStatus agentGetConsentStatus) {
        this.agentGetConfiguration = agentGetConfiguration;
        this.agentGetConsentStatus = agentGetConsentStatus;
    }

    @Override
    public NonInteractionStepResponse<ConsentStatus> execute(StepRequestBase<Void> request) {
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

        return NonInteractionStepResponse.done(consentStatus);
    }
}
