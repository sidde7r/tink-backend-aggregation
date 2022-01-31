package se.tink.agent.sdk.payments.bulk.generic;

import java.util.List;
import se.tink.agent.sdk.models.payments.BulkPaymentSigningBasket;
import se.tink.agent.sdk.models.payments.bulk_payment_register_basket_result.BulkPaymentRegisterBasketResult;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_basket_result.BulkPaymentSignBasketResult;
import se.tink.agent.sdk.models.payments.payment.Payment;
import se.tink.agent.sdk.payments.bulk.steppable_execution.BulkPaymentSignFlow;

public interface GenericBulkPaymentInitiator {
    BulkPaymentRegisterBasketResult registerPayments(List<Payment> payments);

    BulkPaymentSignFlow getSignFlow();

    BulkPaymentSignBasketResult getSignStatus(BulkPaymentSigningBasket basket);
}
