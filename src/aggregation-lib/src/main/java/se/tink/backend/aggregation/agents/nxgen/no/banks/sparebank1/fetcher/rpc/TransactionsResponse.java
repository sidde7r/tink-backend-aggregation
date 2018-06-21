package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsResponse {
    private List<TransactionEntity> transactions;

    public List<TransactionEntity> getTransactions() {
        return transactions == null ? Collections.emptyList() : transactions;
    }
}
