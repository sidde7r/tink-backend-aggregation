package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.TransactionEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionsResponse {
    private List<TransactionEntity> transactions;

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(
            List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }
}
