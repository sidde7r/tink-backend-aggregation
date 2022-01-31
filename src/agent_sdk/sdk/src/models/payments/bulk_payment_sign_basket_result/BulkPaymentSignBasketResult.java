package se.tink.agent.sdk.models.payments.bulk_payment_sign_basket_result;

import java.util.List;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_basket_result.builder.BulkPaymentSignBasketResultBuildAll;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.BulkPaymentSignResult;

public class BulkPaymentSignBasketResult {
    private final List<BulkPaymentSignResult> paymentSignResults;

    BulkPaymentSignBasketResult(List<BulkPaymentSignResult> paymentSignResults) {
        this.paymentSignResults = paymentSignResults;
    }

    public List<BulkPaymentSignResult> getPaymentSignResults() {
        return paymentSignResults;
    }

    public static BulkPaymentSignBasketResultBuildAll builder() {
        return new BulkPaymentSignBasketResultBuilder();
    }
}
