package src.agent_sdk.runtime.src.payments.single;

import se.tink.agent.sdk.models.payments.payment.Payment;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;
import se.tink.agent.sdk.models.payments.single_payment_register_result.SinglePaymentRegisterResult;
import se.tink.agent.sdk.models.payments.single_payment_sign_result.SinglePaymentSignResult;
import se.tink.agent.sdk.payments.single.generic.GenericSinglePaymentInitiator;
import se.tink.agent.sdk.payments.single.steppable_execution.SinglePaymentSignFlow;

public class RuntimeSinglePaymentInitiator {
    private final GenericSinglePaymentInitiator agentSinglePaymentInitiator;

    public RuntimeSinglePaymentInitiator(
            GenericSinglePaymentInitiator agentSinglePaymentInitiator) {
        this.agentSinglePaymentInitiator = agentSinglePaymentInitiator;
    }

    public SinglePaymentRegisterResult registerPayment(Payment payment) {
        return this.agentSinglePaymentInitiator.registerPayment(payment);
    }

    public SinglePaymentSignFlow getSignFlow() {
        return this.agentSinglePaymentInitiator.getSignFlow();
    }

    public SinglePaymentSignResult getSignStatus(PaymentReference paymentReference) {
        return this.agentSinglePaymentInitiator.getSignStatus(paymentReference);
    }
}
