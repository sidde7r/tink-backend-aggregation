package se.tink.agent.sdk.models.payments.bulk_payment_register_basket_result;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.agent.sdk.models.payments.PaymentError;
import se.tink.agent.sdk.models.payments.bulk_payment_register_basket_result.builder.BulkPaymentRegisterBasketResultBuild;
import se.tink.agent.sdk.models.payments.bulk_payment_register_basket_result.builder.BulkPaymentRegisterBasketResultBuildPaymentResult;
import se.tink.agent.sdk.models.payments.bulk_payment_register_basket_result.builder.BulkPaymentRegisterBasketResultBuildPaymentResults;
import se.tink.agent.sdk.models.payments.bulk_payment_register_basket_result.builder.BulkPaymentRegisterBasketResultBuildPaymentResultsAll;
import se.tink.agent.sdk.models.payments.bulk_payment_register_basket_result.builder.BulkPaymentRegisterBasketResultBuildReference;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.BulkPaymentRegisterResult;
import se.tink.agent.sdk.models.payments.payment.Payment;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;
import se.tink.agent.sdk.storage.SerializableReference;

public class BulkPaymentRegisterBasketResultBuilder
        implements BulkPaymentRegisterBasketResultBuildReference,
                BulkPaymentRegisterBasketResultBuildPaymentResult,
                BulkPaymentRegisterBasketResultBuildPaymentResults,
                BulkPaymentRegisterBasketResultBuildPaymentResultsAll,
                BulkPaymentRegisterBasketResultBuild {

    private SerializableReference bankBasketReference;
    private List<BulkPaymentRegisterResult> paymentRegisterResults;

    BulkPaymentRegisterBasketResultBuilder() {
        this.bankBasketReference = null;
        this.paymentRegisterResults = new ArrayList<>();
    }

    @Override
    public BulkPaymentRegisterBasketResultBuildPaymentResultsAll basketReference(String reference) {
        this.bankBasketReference = SerializableReference.from(reference);
        return this;
    }

    @Override
    public BulkPaymentRegisterBasketResultBuildPaymentResultsAll basketReference(Object reference) {
        this.bankBasketReference = SerializableReference.from(reference);
        return this;
    }

    @Override
    public BulkPaymentRegisterBasketResultBuildPaymentResults noBasketReference() {
        this.bankBasketReference = null;
        return this;
    }

    @Override
    public BulkPaymentRegisterBasketResultBuild allSuccessful(List<Payment> payments) {
        this.paymentRegisterResults =
                payments.stream()
                        .map(
                                payment ->
                                        BulkPaymentRegisterResult.builder()
                                                .reference(
                                                        PaymentReference.builder()
                                                                .payment(payment)
                                                                .noBankReference()
                                                                .build())
                                                .noError()
                                                .build())
                        .collect(Collectors.toList());
        return this;
    }

    @Override
    public BulkPaymentRegisterBasketResultBuild allWithSameError(
            List<Payment> payments, PaymentError error) {
        this.paymentRegisterResults =
                payments.stream()
                        .map(
                                payment ->
                                        BulkPaymentRegisterResult.builder()
                                                .reference(
                                                        PaymentReference.builder()
                                                                .payment(payment)
                                                                .noBankReference()
                                                                .build())
                                                .error(error)
                                                .build())
                        .collect(Collectors.toList());
        return this;
    }

    @Override
    public BulkPaymentRegisterBasketResultBuildPaymentResult paymentResult(
            BulkPaymentRegisterResult paymentRegisterResult) {
        this.paymentRegisterResults.add(paymentRegisterResult);
        return this;
    }

    @Override
    public BulkPaymentRegisterBasketResultBuild paymentResults(
            List<BulkPaymentRegisterResult> paymentRegisterResults) {
        this.paymentRegisterResults = new ArrayList<>(paymentRegisterResults);
        return this;
    }

    @Override
    public BulkPaymentRegisterBasketResult build() {
        return new BulkPaymentRegisterBasketResult(
                this.bankBasketReference, this.paymentRegisterResults);
    }
}
