package se.tink.backend.aggregation.agents.banks.crosskey.responses;

import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.system.rpc.Transaction;

public class TransactionsResponse extends BaseResponse {
    public List<TransactionResponse> transactions;

    public List<Transaction> toTinkTransactions() throws Exception {
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
