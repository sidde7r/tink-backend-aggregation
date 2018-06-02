package se.tink.backend.system.workers.processor.deduplication;

import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.core.Transaction;

public class DeduplicationResult {
    private final List<Transaction> transactionsToSave;
    private final List<Transaction> transactionsToDelete;

    public DeduplicationResult(List<Transaction> newTransactions, List<Transaction> transactionsToUpdate,
            List<Transaction> transactionsToDelete) {
        this.transactionsToSave = newTransactions;
        this.transactionsToSave.addAll(transactionsToUpdate);
        this.transactionsToDelete = transactionsToDelete;
    }

    public List<Transaction> getTransactionsToSave() {
        return transactionsToSave;
    }

    public List<Transaction> getTransactionsToDelete() {
        return transactionsToDelete;
    }

    public static DeduplicationResult empty() {
        return new DeduplicationResult(
                Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList());
    }
}
