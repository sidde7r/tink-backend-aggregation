package se.tink.agent.sdk.models.payments.bulk_payment_sign_result.builder;

import se.tink.agent.sdk.models.payments.payment.Debtor;

public interface BulkPaymentSignResultBuildDebtor {
    BulkPaymentSignResultBuild debtor(Debtor debtor);

    BulkPaymentSignResultBuild noDebtor();
}
