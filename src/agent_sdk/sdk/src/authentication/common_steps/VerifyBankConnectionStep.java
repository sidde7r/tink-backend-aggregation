package se.tink.agent.sdk.authentication.common_steps;

import se.tink.agent.sdk.authentication.consent.ConsentStatus;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequestBase;
import se.tink.agent.sdk.steppable_execution.non_interactive_step.NonInteractionStepResponse;
import se.tink.agent.sdk.steppable_execution.non_interactive_step.NonInteractiveStep;

public class VerifyBankConnectionStep extends NonInteractiveStep<ConsentStatus> {
    private final VerifyBankConnection agentVerifyBankConnection;

    public VerifyBankConnectionStep(VerifyBankConnection agentVerifyBankConnection) {
        this.agentVerifyBankConnection = agentVerifyBankConnection;
    }

    @Override
    public NonInteractionStepResponse<ConsentStatus> execute(StepRequestBase request) {
        ConsentStatus consentStatus = agentVerifyBankConnection.verifyBankConnection();
        return NonInteractionStepResponse.done(consentStatus);
    }
}
