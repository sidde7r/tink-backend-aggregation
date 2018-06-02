package se.tink.backend.system.workers.processor.deduplication;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import java.util.Collection;
import java.util.List;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.TransactionProcessorUserData;
import se.tink.backend.system.workers.processor.deduplication.detector.DeterministicTransactionDeduplicator;
import se.tink.backend.system.workers.processor.deduplication.detector.FuzzyTransactionDeduplicator;
import se.tink.libraries.metrics.MetricRegistry;

public class GlobalDeduplicationCommand implements TransactionProcessorCommand {
    private final MetricRegistry metricRegistry;

    private final TransactionProcessorContext context;

    public GlobalDeduplicationCommand(MetricRegistry metricRegistry,
            TransactionProcessorContext context) {
        this.metricRegistry = metricRegistry;
        this.context = context;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }

    @Override
    public TransactionProcessorCommandResult initialize() {
        TransactionProcessorUserData userData = context.getUserData();

        boolean allTrxInBatchHaveExternalId = context.getInBatchTransactions().stream()
                .noneMatch(t -> Strings.isNullOrEmpty(t.getPayloadValue(TransactionPayloadTypes.EXTERNAL_ID)));

        DeduplicationResult result = allTrxInBatchHaveExternalId ?
                deterministicDeduplication(userData) :
                fuzzyDeduplication(userData);

        updateContext(context, result.getTransactionsToSave(), result.getTransactionsToDelete());

        return TransactionProcessorCommandResult.CONTINUE;
    }

    private DeduplicationResult deterministicDeduplication(TransactionProcessorUserData userData) {
        DeterministicTransactionDeduplicator deduplicator = new DeterministicTransactionDeduplicator(metricRegistry);

        Collection<Transaction> inStoreTransactions = userData.getInStoreTransactions().values();
        List<Transaction> inBatchTransactions = context.getInBatchTransactions();

        return deduplicator.deduplicate(inStoreTransactions, inBatchTransactions);
    }


    private DeduplicationResult fuzzyDeduplication(TransactionProcessorUserData userData) {
        FuzzyTransactionDeduplicator deduplicator = new FuzzyTransactionDeduplicator(metricRegistry,
                context.getProvider(), userData.getAccounts());

        Collection<Transaction> inStoreTransactions = userData.getInStoreTransactions().values();
        List<Transaction> inBatchTransactions = context.getInBatchTransactions();

        return deduplicator.deduplicate(inStoreTransactions, inBatchTransactions);
    }

    private void updateContext(
            TransactionProcessorContext context,
            List<Transaction> transactionsToSave,
            List<Transaction> transactionsToDelete
    ) {
        context.updateInBatchTransactions(transactionsToSave);

        for (Transaction transactionToSave : transactionsToSave) {
            context.getTransactionsToSave().put(transactionToSave.getId(), transactionToSave);
        }

        for (Transaction transactionToDelete : transactionsToDelete) {
            context.addTransactionToDelete(transactionToDelete);
        }
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {
        return TransactionProcessorCommandResult.CONTINUE;
    }

    /**
     * Called for every command in command chain's reverse order at after processing all transactions.
     */
    @Override
    public void postProcess() {
        // Deliberately left empty.
    }

}
