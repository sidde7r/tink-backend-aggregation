package se.tink.backend.system.workers.processor.storage;

import com.google.common.base.MoreObjects;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import se.tink.libraries.metrics.Histogram;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.workers.processor.TransactionProcessor;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;

/**
 * Temporary command that contains legacy logic from SaveTransactionCommand
 * The new TransactionProcessor command chain doesn't need this logic, while the old command chains do.
 * So to keep the old command chains from breaking while still in use, this command is required.
 *
 * However, we should remove this command as soon as we have migrated everything (including connectors)
 * to run with the new FuzzyTransactionDeduplicator command.
 */
@Deprecated
public class PrepareTransactionsToSaveAndDeleteCommand implements TransactionProcessorCommand {
    private Histogram unchangedTransactionsHistogram;
    private Histogram updatedTransactionsHistogram;
    private Histogram newTransactionsHistogram;
    private Histogram deletedTransactionsHistogram;

    private int processedTransactionsCount;
    private final MetricRegistry metricRegistry;
    private final TransactionProcessorContext context;

    public PrepareTransactionsToSaveAndDeleteCommand(
            TransactionProcessorContext context, MetricRegistry metricRegistry) {
        this.context = context;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public TransactionProcessorCommandResult initialize() {
        this.processedTransactionsCount = context.getInBatchTransactions().size();

        MetricId.MetricLabels providerLabel = new MetricId.MetricLabels()
                .add(TransactionProcessor.MetricKey.PROVIDER, context.getProvider().getName());

        this.unchangedTransactionsHistogram = metricRegistry.histogram(
                TransactionProcessor.UNCHANGED_TRANSACTIONS_METRIC_ID.label(providerLabel),
                TransactionProcessor.MetricBuckets.X_LARGE);
        this.updatedTransactionsHistogram = metricRegistry.histogram(
                TransactionProcessor.UPDATED_TRANSACTIONS_METRIC_ID.label(providerLabel),
                TransactionProcessor.MetricBuckets.MEDIUM);
        this.newTransactionsHistogram = metricRegistry.histogram(
                TransactionProcessor.NEW_TRANSACTIONS_METRIC_ID.label(providerLabel),
                TransactionProcessor.MetricBuckets.LARGE);
        this.deletedTransactionsHistogram = metricRegistry.histogram(
                TransactionProcessor.DELETED_TRANSACTIONS_METRIC_ID.label(providerLabel),
                TransactionProcessor.MetricBuckets.SMALL);

        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {
        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() {
        HashMap<String, Transaction> transactionsToSave = context.getTransactionsToSave();
        int newTransactionsCount = transactionsToSave.size();

        Set<String> updateList = context.getTransactionsToUpdateList();
        updatedTransactionsHistogram.update(updateList.size());

        for (String id : updateList) {
            if (transactionsToSave.containsKey(id)) {
                // if we have this transaction here, don't override it
                // transactionsToSave included a transaction that were updated = decrement the counter
                newTransactionsCount--;
                continue;
            }
            Transaction t = context.getUserData().getInStoreTransaction(id);

            if (t != null) {
                transactionsToSave.put(t.getId(), t);
            }
        }

        // Remove the delete marked transactions.

        List<Transaction> transactionsToDelete = context.getTransactionsToDelete();
        deletedTransactionsHistogram.update(transactionsToDelete.size());

        for (Transaction transactionToDelete : transactionsToDelete) {
            if (transactionsToSave.containsKey(transactionToDelete.getId())) {
                // transactionsToSave included a transaction that should be removed = decrement the counter
                newTransactionsCount--;
                transactionsToSave.remove(transactionToDelete.getId());
            }
        }

        // Safe to update newTransactionsHistogram since updated & deleted transactions have been excluded from the count
        newTransactionsHistogram.update(newTransactionsCount);
        unchangedTransactionsHistogram.update(processedTransactionsCount - transactionsToSave.size());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }
}
