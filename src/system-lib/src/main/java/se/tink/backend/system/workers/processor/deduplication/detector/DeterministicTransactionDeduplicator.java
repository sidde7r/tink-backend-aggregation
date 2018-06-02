package se.tink.backend.system.workers.processor.deduplication.detector;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.system.workers.processor.deduplication.DeduplicationResult;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class DeterministicTransactionDeduplicator {

    private static final LogUtils log = new LogUtils(DeterministicTransactionDeduplicator.class);

    private static final MetricId DEDUPLICATOR_METRIC_ID = MetricId.newId("transaction_deterministic_deduplicator");

    private final Counter successCounter;
    private final Counter failedCounter;

    public DeterministicTransactionDeduplicator(MetricRegistry metricRegistry) {
        this.successCounter = metricRegistry.meter(DEDUPLICATOR_METRIC_ID.label("outcome", "success"));
        this.failedCounter = metricRegistry.meter(DEDUPLICATOR_METRIC_ID.label("outcome", "failed"));
    }

    public DeduplicationResult deduplicate(Collection<Transaction> inStoreTransactions,
            List<Transaction> inBatchTransactions) {

        try {
            DeterministicPendingTransactionMatcher pendingMatcher = new DeterministicPendingTransactionMatcher(
                    inStoreTransactions,
                    inBatchTransactions);

            DeduplicationResult result = calculateDeduplications(
                    pendingMatcher,
                    toMapByExternalIdAndAccountId(inStoreTransactions),
                    toMapByExternalIdAndAccountId(inBatchTransactions));

            successCounter.inc();

            return result;
        } catch (RuntimeException e) {
            log.error("Failed deduplication due to exception", e);
            failedCounter.inc();
            throw e;
        }
    }

    private DeduplicationResult calculateDeduplications(
            DeterministicPendingTransactionMatcher pendingMatcher,
            Map<String, Map<String, Transaction>> inStoreByExternalIdByAccountId,
            Map<String, Map<String, Transaction>> inBatchByExternalIdByAccountId) {

        List<Transaction> toAdd = Lists.newArrayList();
        List<Transaction> toUpdate = Lists.newArrayList();
        List<Transaction> toRemove = Lists.newArrayList();

        for (String accountId : inBatchByExternalIdByAccountId.keySet()) {
            Map<String, Transaction> incomingTransactionsByExternalId = inBatchByExternalIdByAccountId.get(accountId);
            Map<String, Transaction> existingTransactionsByExternalId = inStoreByExternalIdByAccountId.get(accountId);

            for (String externalId : incomingTransactionsByExternalId.keySet()) {
                Transaction incomingTransaction = incomingTransactionsByExternalId.get(externalId);

                if (existingTransactionsByExternalId == null || !existingTransactionsByExternalId
                        .containsKey(externalId)) {

                    if (pendingMatcher.hasPendingTransactionsInStore(accountId) && !incomingTransaction.isPending()) {
                        // We have pending in store for this account and incoming transaction is not pending
                        // matchAndUpdate does update the incomingTransaction with the correct category
                        toRemove.addAll(pendingMatcher.matchAndUpdate(incomingTransaction));
                        toRemove.addAll(pendingMatcher.findExpiredPending(accountId, toRemove));
                    }

                    // The externalId was not found, it's a new transaction.
                    toAdd.add(incomingTransaction);
                }

                // The deduplication happens automatically here -- the transactions that is not put on either
                // toAdd or toUpdate will not go through the transaction processor chain.
            }
        }

        return new DeduplicationResult(toAdd, toUpdate, toRemove);
    }

    private static Map<String, Map<String, Transaction>> toMapByExternalIdAndAccountId(
            Collection<Transaction> transactions) {
        if (transactions == null) {
            return Maps.newHashMap();
        }

        Map<String, Map<String, Transaction>> result = Maps.newHashMap();

        for (Transaction transaction : transactions) {
            if (!result.containsKey(transaction.getAccountId())) {
                result.put(transaction.getAccountId(), Maps.newHashMap());
            }

            result.get(transaction.getAccountId())
                    .put(transaction.getPayloadValue(TransactionPayloadTypes.EXTERNAL_ID), transaction);
        }

        return result;
    }
}
