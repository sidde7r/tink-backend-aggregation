package se.tink.backend.aggregation.agents.banks.crosskey.responses;

import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.aggregation.agents.models.Transaction;

public class TransactionsResponse extends BaseResponse {
    private List<TransactionResponse> transactions;

    public List<Transaction> toTinkTransactions() {
        List<Transaction> tinkTransactions = Lists.newArrayList();

        for (TransactionResponse transaction : getTransactions()) {
            tinkTransactions.add(transaction.toTinkTransaction());
        }

        return tinkTransactions;
    }

    public List<TransactionResponse> getTransactions() {
        return transactions != null ? transactions : Lists.<TransactionResponse>newArrayList();
    }
}
