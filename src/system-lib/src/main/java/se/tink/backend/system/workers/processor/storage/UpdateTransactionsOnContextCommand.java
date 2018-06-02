package se.tink.backend.system.workers.processor.storage;

import com.google.common.base.MoreObjects;
import java.util.HashMap;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;

public class UpdateTransactionsOnContextCommand implements TransactionProcessorCommand {

    private final TransactionProcessorContext context;

    public UpdateTransactionsOnContextCommand(
            TransactionProcessorContext context) {
        this.context = context;
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {
        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() {
        final HashMap<String, Transaction> storeTransactions = context.getUserData().getInStoreTransactions();

        // Add new/updated transactions to the in-store map
        context.getTransactionsToSave().values().forEach(t -> storeTransactions.put(t.getId(), t));

        // Remove deleted transactions from the in-store map
        context.getTransactionsToDelete().forEach(t -> storeTransactions.remove(t.getId()));
    }

    @Override
    public TransactionProcessorCommandResult initialize() {
        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }
}
