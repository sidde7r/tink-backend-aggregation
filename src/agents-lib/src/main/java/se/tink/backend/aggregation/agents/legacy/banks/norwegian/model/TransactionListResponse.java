package se.tink.backend.aggregation.agents.banks.norwegian.model;

import com.google.common.collect.Lists;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.models.Transaction;

public class TransactionListResponse extends ArrayList<TransactionEntity> {

    public List<Transaction> getTransactions() throws ParseException {
        List<Transaction> transactions = Lists.newArrayList();
        for (TransactionEntity transactionEntity : this) {
            transactions.add(transactionEntity.toTransaction());
        }

        return transactions;
    }
}
