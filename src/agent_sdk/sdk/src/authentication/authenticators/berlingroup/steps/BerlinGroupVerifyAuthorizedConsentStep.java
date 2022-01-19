package se.tink.agent.sdk.authentication.authenticators.berlingroup.steps;

import java.time.Duration;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupAuthenticatorConfiguration;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupGetConfiguration;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupGetConsentStatus;
import se.tink.agent.sdk.authentication.base_steps.NewConsentStep;
import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.consent.ConsentStatus;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.InteractiveStepResponse;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;

public class BerlinGroupVerifyAuthorizedConsentStep extends NewConsentStep {

    private final BerlinGroupGetConfiguration agentGetConfiguration;
    private final BerlinGroupGetConsentStatus agentGetConsentStatus;

    public BerlinGroupVerifyAuthorizedConsentStep(
            BerlinGroupGetConfiguration agentGetConfiguration,
            BerlinGroupGetConsentStatus agentGetConsentStatus) {
        this.agentGetConfiguration = agentGetConfiguration;
        this.agentGetConsentStatus = agentGetConsentStatus;
    }

    @Override
    public InteractiveStepResponse<ConsentLifetime> execute(StepRequest<Void> request) {
        if (!request.getUserResponseData().isPresent()) {
            throw ThirdPartyAppError.TIMED_OUT.exception();
        }

        String consentId =
                request.getStepStorage()
                        .tryGet(BerlinGroupAuthenticator.STATE_KEY_CONSENT_ID)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "ConsentId was not present in StepStorage."));

        ConsentStatus consentStatus = agentGetConsentStatus.getConsentStatus(consentId);
        if (!ConsentStatus.VALID.equals(consentStatus)) {
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
        }

        // Write the `consentId` into the agent storage on a key picked by the agent.
        BerlinGroupAuthenticatorConfiguration configuration =
                this.agentGetConfiguration.getConfiguration();
        request.getAgentStorage().put(configuration.getConsentIdStorageKey(), consentId);

        Duration consentLifetime =
                Duration.ofDays(configuration.getConsentValidForPeriod().getDays());
        return InteractiveStepResponse.done(ConsentLifetime.specificLifetime(consentLifetime));
    }
}
