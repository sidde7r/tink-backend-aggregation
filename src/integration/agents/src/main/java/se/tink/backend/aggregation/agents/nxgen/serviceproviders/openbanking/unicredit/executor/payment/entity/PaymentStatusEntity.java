package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentStatusEntity {

    private String transactionStatus;
    private String paymentId;

    public String getTransactionStatus() {
        return transactionStatus;
    }
}
