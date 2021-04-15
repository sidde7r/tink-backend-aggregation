package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher.data;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsResponse {

    private Transactions transactions;

    public List<TransactionEntity> getTransactions() {
        return transactions == null ? Collections.emptyList() : transactions.getTransactions();
    }
}
