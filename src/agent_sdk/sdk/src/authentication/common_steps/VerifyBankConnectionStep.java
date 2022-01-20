package se.tink.agent.sdk.authentication.common_steps;

import se.tink.agent.sdk.authentication.consent.ConsentStatus;
import se.tink.agent.sdk.authentication.steppable_execution.ExistingConsentStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequestBase;
import se.tink.agent.sdk.steppable_execution.non_interactive_step.NonInteractionStepResponse;

public class VerifyBankConnectionStep extends ExistingConsentStep {
    private final VerifyBankConnection agentVerifyBankConnection;

    public VerifyBankConnectionStep(VerifyBankConnection agentVerifyBankConnection) {
        this.agentVerifyBankConnection = agentVerifyBankConnection;
    }

    @Override
    public NonInteractionStepResponse<ConsentStatus> execute(StepRequestBase<Void> request) {
        ConsentStatus consentStatus = agentVerifyBankConnection.verifyBankConnection();
        return NonInteractionStepResponse.done(consentStatus);
    }
}
