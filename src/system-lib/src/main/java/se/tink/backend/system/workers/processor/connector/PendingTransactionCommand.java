package se.tink.backend.system.workers.processor.connector;

import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.deduplication.detector.DeterministicPendingTransactionMatcher;
import se.tink.backend.system.workers.processor.deduplication.DeduplicationResult;

public class PendingTransactionCommand implements TransactionProcessorCommand {

    private static final LogUtils log = new LogUtils(PendingTransactionCommand.class);
    private TransactionProcessorContext context;
    private DeterministicPendingTransactionMatcher matcher;

    public PendingTransactionCommand(TransactionProcessorContext context) {
        this.context = context;
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {
        // Do nothing here.
        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public TransactionProcessorCommandResult initialize() {
        matcher = new DeterministicPendingTransactionMatcher(
                context.getUserData().getInStoreTransactions().values(),
                context.getInBatchTransactions());

        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() {
        if (matcher.hasPendingTransactionsInStore()) {
            DeduplicationResult result = matcher.matchAndUpdate();

            log.info(context.getUser().getId(),
                    String.format("Found in total %s pending transactions that was replaced by %s non-pending transactions.",
                            result.getTransactionsToDelete(), result.getTransactionsToSave()));

            for (Transaction toUpdate : result.getTransactionsToSave()) {
                context.addTransactionToUpdateListPresentInDb(toUpdate.getId());
            }

            for (Transaction pendingTransaction : result.getTransactionsToDelete()) {
                context.addTransactionToDelete(pendingTransaction);
            }
        }
    }
}
