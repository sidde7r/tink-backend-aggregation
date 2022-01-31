package se.tink.agent.sdk.payments.beneficiary.steppable_execution;

import se.tink.agent.sdk.models.payments.BeneficiaryReference;
import se.tink.agent.sdk.models.payments.BeneficiaryState;
import se.tink.agent.sdk.steppable_execution.interactive_step.InteractiveStep;

public abstract class BeneficiarySignStep
        extends InteractiveStep<BeneficiaryReference, BeneficiaryState> {}
