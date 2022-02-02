package se.tink.agent.sdk.payments.single.steppable_execution;

import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;
import se.tink.agent.sdk.models.payments.single_payment_sign_result.SinglePaymentSignResult;
import se.tink.agent.sdk.steppable_execution.interactive_step.InteractiveStep;

public abstract class SinglePaymentSignStep
        extends InteractiveStep<PaymentReference, SinglePaymentSignResult> {}
