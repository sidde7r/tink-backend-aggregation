package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetPaymentStatusResponse {
    private String transactionStatus;

    public String getTransactionStatus() {
        return transactionStatus;
    }
}
