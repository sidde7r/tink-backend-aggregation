package se.tink.agent.sdk.models.payments.bulk_payment_sign_basket_result;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.agent.sdk.models.payments.BulkPaymentSigningBasket;
import se.tink.agent.sdk.models.payments.PaymentError;
import se.tink.agent.sdk.models.payments.PaymentStatus;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_basket_result.builder.BulkPaymentSignBasketResultBuild;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_basket_result.builder.BulkPaymentSignBasketResultBuildAll;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_basket_result.builder.BulkPaymentSignBasketResultBuildOne;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.BulkPaymentSignResult;

public class BulkPaymentSignBasketResultBuilder
        implements BulkPaymentSignBasketResultBuildAll,
                BulkPaymentSignBasketResultBuildOne,
                BulkPaymentSignBasketResultBuild {

    private List<BulkPaymentSignResult> paymentSignResults;

    BulkPaymentSignBasketResultBuilder() {
        this.paymentSignResults = new ArrayList<>();
    }

    @Override
    public BulkPaymentSignBasketResultBuild allWithSameStatus(
            BulkPaymentSigningBasket signingBasket, PaymentStatus status) {
        this.paymentSignResults =
                signingBasket.getPaymentReferences().stream()
                        .map(
                                paymentReference ->
                                        BulkPaymentSignResult.builder()
                                                .reference(paymentReference)
                                                .status(status)
                                                .noDebtor()
                                                .build())
                        .collect(Collectors.toList());

        return this;
    }

    @Override
    public BulkPaymentSignBasketResultBuild allWithSameError(
            BulkPaymentSigningBasket signingBasket, PaymentError error) {
        this.paymentSignResults =
                signingBasket.getPaymentReferences().stream()
                        .map(
                                paymentReference ->
                                        BulkPaymentSignResult.builder()
                                                .reference(paymentReference)
                                                .error(error)
                                                .noDebtor()
                                                .build())
                        .collect(Collectors.toList());

        return this;
    }

    @Override
    public BulkPaymentSignBasketResultBuild paymentResults(
            List<BulkPaymentSignResult> paymentSignResults) {
        this.paymentSignResults = new ArrayList<>(paymentSignResults);
        return this;
    }

    @Override
    public BulkPaymentSignBasketResultBuildOne paymentResult(
            BulkPaymentSignResult paymentSignResult) {
        this.paymentSignResults.add(paymentSignResult);
        return this;
    }

    @Override
    public BulkPaymentSignBasketResult build() {
        return new BulkPaymentSignBasketResult(this.paymentSignResults);
    }
}
