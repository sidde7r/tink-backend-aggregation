package se.tink.agent.sdk.payments.bulk.steppable_execution;

import se.tink.agent.sdk.models.payments.BulkPaymentSigningBasket;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_basket_result.BulkPaymentSignBasketResult;
import se.tink.agent.sdk.steppable_execution.interactive_step.InteractiveStep;

public abstract class BulkPaymentSignStep
        extends InteractiveStep<BulkPaymentSigningBasket, BulkPaymentSignBasketResult> {}
