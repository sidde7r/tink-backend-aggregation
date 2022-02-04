package se.tink.agent.runtime.payments.bulk;

import java.util.List;
import se.tink.agent.sdk.models.payments.BulkPaymentSigningBasket;
import se.tink.agent.sdk.models.payments.bulk_payment_register_basket_result.BulkPaymentRegisterBasketResult;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_basket_result.BulkPaymentSignBasketResult;
import se.tink.agent.sdk.models.payments.payment.Payment;
import se.tink.agent.sdk.payments.bulk.generic.GenericBulkPaymentInitiator;
import se.tink.agent.sdk.payments.bulk.steppable_execution.BulkPaymentSignFlow;

public class RuntimeBulkPaymentInitiator {
    private final GenericBulkPaymentInitiator agentBulkPaymentInitiator;

    public RuntimeBulkPaymentInitiator(GenericBulkPaymentInitiator agentBulkPaymentInitiator) {
        this.agentBulkPaymentInitiator = agentBulkPaymentInitiator;
    }

    public BulkPaymentRegisterBasketResult registerPayments(List<Payment> payments) {
        return this.agentBulkPaymentInitiator.registerPayments(payments);
    }

    public BulkPaymentSignFlow getSignFlow() {
        return this.agentBulkPaymentInitiator.getSignFlow();
    }

    public BulkPaymentSignBasketResult getSignStatus(BulkPaymentSigningBasket basket) {
        return this.agentBulkPaymentInitiator.getSignStatus(basket);
    }
}
