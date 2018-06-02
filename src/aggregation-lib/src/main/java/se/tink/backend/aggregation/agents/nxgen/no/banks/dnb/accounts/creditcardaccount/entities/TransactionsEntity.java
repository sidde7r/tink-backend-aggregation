package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsEntity {
    private List<TransactionEntity> transactions;
    private String transactionDate;

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public String getTransactionDate() {
        return transactionDate;
    }
}
