package se.tink.agent.agents.example.payments;

import se.tink.agent.agents.example.payments.steps.ExampleSinglePaymentBankIdSignStep;
import se.tink.agent.sdk.models.payments.PaymentStatus;
import se.tink.agent.sdk.models.payments.payment.Payment;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;
import se.tink.agent.sdk.models.payments.single_payment_register_result.SinglePaymentRegisterResult;
import se.tink.agent.sdk.models.payments.single_payment_sign_result.SinglePaymentSignResult;
import se.tink.agent.sdk.payments.single.generic.GenericSinglePaymentInitiator;
import se.tink.agent.sdk.payments.single.steppable_execution.SinglePaymentSignFlow;

public class ExampleSinglePaymentInitiator implements GenericSinglePaymentInitiator {
    @Override
    public SinglePaymentRegisterResult registerPayment(Payment payment) {
        String someBankIdFromApiResponse = "foobar";

        return SinglePaymentRegisterResult.builder()
                .noError()
                .bankReference(someBankIdFromApiResponse)
                .build();
    }

    @Override
    public SinglePaymentSignFlow getSignFlow() {
        return SinglePaymentSignFlow.builder()
                .startStep(new ExampleSinglePaymentBankIdSignStep())
                .build();
    }

    @Override
    public SinglePaymentSignResult getSignStatus(PaymentReference paymentReference) {
        return SinglePaymentSignResult.builder()
                .status(PaymentStatus.INITIATED_AND_EXECUTED)
                .noDebtor()
                .build();
    }
}
