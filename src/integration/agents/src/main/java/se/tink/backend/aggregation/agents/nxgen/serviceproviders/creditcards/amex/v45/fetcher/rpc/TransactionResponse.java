package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionResponse {
    private TransactionDetailsEntity transactionDetails;

    public TransactionDetailsEntity getTransactionDetails() {
        return transactionDetails;
    }
}
