package se.tink.backend.aggregation.utils;

import java.util.Comparator;
import se.tink.backend.aggregation.agents.models.Transaction;

public class TransactionOrdering {
    /**
     * Standard transaction ordering based on date and when the transaction was inserted into the database.
     */
    public static final Comparator<Transaction> TRANSACTION_DATE_ORDERING = Comparator
            .comparing(Transaction::getDate)
            .thenComparing(Transaction::getTimestamp)
            .thenComparing(Transaction::getId);
}
