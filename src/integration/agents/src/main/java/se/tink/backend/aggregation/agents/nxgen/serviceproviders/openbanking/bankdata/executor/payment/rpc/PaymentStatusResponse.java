package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentStatusResponse {
    private String transactionStatus;

    public String getTransactionStatus() {
        return transactionStatus;
    }
}
