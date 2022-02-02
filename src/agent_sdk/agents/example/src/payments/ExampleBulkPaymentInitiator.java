package se.tink.agent.agents.example.payments;

import java.util.List;
import se.tink.agent.agents.example.payments.steps.ExampleBulkPaymentBankIdSignStep;
import se.tink.agent.sdk.models.payments.BulkPaymentSigningBasket;
import se.tink.agent.sdk.models.payments.PaymentError;
import se.tink.agent.sdk.models.payments.bulk_payment_register_basket_result.BulkPaymentRegisterBasketResult;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.BulkPaymentRegisterResult;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_basket_result.BulkPaymentSignBasketResult;
import se.tink.agent.sdk.models.payments.payment.Payment;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;
import se.tink.agent.sdk.payments.bulk.generic.GenericBulkPaymentInitiator;
import se.tink.agent.sdk.payments.bulk.steppable_execution.BulkPaymentSignFlow;

public class ExampleBulkPaymentInitiator implements GenericBulkPaymentInitiator {

    @Override
    public BulkPaymentRegisterBasketResult registerPayments(List<Payment> payments) {

        BulkPaymentRegisterBasketResult.builder()
                .basketReference("hejsan")
                .paymentResult(
                        BulkPaymentRegisterResult.builder()
                                .reference(
                                        PaymentReference.builder()
                                                .payment(null)
                                                .bankReference("hejsan")
                                                .build())
                                .noError()
                                .build())
                .build();

        return null;
    }

    @Override
    public BulkPaymentSignFlow getSignFlow() {
        return BulkPaymentSignFlow.builder()
                .startStep(new ExampleBulkPaymentBankIdSignStep())
                .build();
    }

    @Override
    public BulkPaymentSignBasketResult getSignStatus(BulkPaymentSigningBasket basket) {
        return BulkPaymentSignBasketResult.builder()
                .allWithSameError(basket, PaymentError.AUTHORIZATION_CANCELLED)
                .build();
    }
}
