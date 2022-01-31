package se.tink.agent.sdk.payments.single.generic;

import com.google.common.annotations.Beta;
import se.tink.agent.sdk.models.payments.payment.Payment;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;
import se.tink.agent.sdk.models.payments.single_payment_register_result.SinglePaymentRegisterResult;
import se.tink.agent.sdk.models.payments.single_payment_sign_result.SinglePaymentSignResult;
import se.tink.agent.sdk.payments.single.steppable_execution.SinglePaymentSignFlow;

@Beta
public interface GenericSinglePaymentInitiator {
    SinglePaymentRegisterResult registerPayment(Payment payment);

    SinglePaymentSignFlow getSignFlow();

    SinglePaymentSignResult getSignStatus(PaymentReference paymentReference);
}
