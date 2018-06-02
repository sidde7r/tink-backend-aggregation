package se.tink.backend.system.workers.processor.deduplication;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.deduplication.detector.DuplicateTransactionDetector;

@Deprecated
public class DeduplicationCommand implements TransactionProcessorCommand {

    private final TransactionProcessorContext context;

    private DuplicateTransactionDetector detector;

    public DeduplicationCommand(TransactionProcessorContext context) {
        this.context = context;
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {
        Optional<Transaction> duplicateTransaction = detector.findAndRemoveDuplicate(transaction);

        if (duplicateTransaction.isPresent()) {
            return TransactionProcessorCommandResult.BREAK;
        } else {
            return TransactionProcessorCommandResult.CONTINUE;
        }
    }

    @Override
    public TransactionProcessorCommandResult initialize() {
        detector = new DuplicateTransactionDetector(context.getUserData().getInStoreTransactions().values());

        return TransactionProcessorCommandResult.CONTINUE;
    }

    /**
     * Called for every command in command chain's reverse order at after processing all transactions.
     */
    @Override
    public void postProcess() {
        // Deliberately left empty.
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }
}
