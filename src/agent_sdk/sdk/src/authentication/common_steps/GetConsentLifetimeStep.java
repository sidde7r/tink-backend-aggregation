package se.tink.agent.sdk.authentication.common_steps;

import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.steppable_execution.NewConsentStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.InteractiveStepResponse;

public class GetConsentLifetimeStep extends NewConsentStep {
    private final GetConsentLifetime agentGetConsentLifetime;

    public GetConsentLifetimeStep(GetConsentLifetime agentGetConsentLifetime) {
        this.agentGetConsentLifetime = agentGetConsentLifetime;
    }

    @Override
    public InteractiveStepResponse<ConsentLifetime> execute(StepRequest<Void> request) {
        ConsentLifetime consentLifetime = this.agentGetConsentLifetime.getConsentLifetime();
        return InteractiveStepResponse.done(consentLifetime);
    }
}
