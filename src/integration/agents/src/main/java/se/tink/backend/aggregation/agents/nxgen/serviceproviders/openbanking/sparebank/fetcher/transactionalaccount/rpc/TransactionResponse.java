package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionResponse {

    private TransactionEntity transactions;

    public TransactionResponse() {
        transactions = new TransactionEntity();
    }

    public TransactionEntity getTransactions() {
        return transactions;
    }
}
