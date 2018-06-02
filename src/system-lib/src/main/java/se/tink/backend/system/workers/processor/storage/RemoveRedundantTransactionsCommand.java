package se.tink.backend.system.workers.processor.storage;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang.time.DateUtils;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.libraries.metrics.Histogram;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.system.workers.processor.TransactionProcessor;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.deduplication.detector.DuplicateTransactionDetector;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Orderings;

/**
 * Removes transactions that for some reason still are in the data-store but should not.
 */
public class RemoveRedundantTransactionsCommand implements TransactionProcessorCommand {
    private static final LogUtils log = new LogUtils(RemoveRedundantTransactionsCommand.class);
    private static final ImmutableSet<String> NON_APPLICABLE_PROVIDERS = ImmutableSet.of(AbnAmroUtils.ABN_AMRO_PROVIDER_NAME);

    private final TransactionProcessorContext context;

    private Histogram redundantTransactionsHistogram;
    private final TransactionDao indexedTransactionDAO;
    private Provider provider;

    public RemoveRedundantTransactionsCommand(
            TransactionProcessorContext context, MetricRegistry metricRegistry,
            TransactionDao indexedTransactionDAO,
            Provider provider) {
        this.context = context;
        this.indexedTransactionDAO = indexedTransactionDAO;
        this.provider = provider;

        this.redundantTransactionsHistogram = metricRegistry.histogram(
                TransactionProcessor.DELETED_TRANSACTIONS_METRIC_ID.label(
                        TransactionProcessor.MetricKey.PROVIDER,
                        provider.getName()
                ),
                TransactionProcessor.MetricBuckets.SMALL
        );
    }

    @Override
    public TransactionProcessorCommandResult initialize() {
        return TransactionProcessorCommandResult.CONTINUE;
    }

    private void checkAndDeleteRedundantTransactions(Iterable<Transaction> batchTransactions,
            Iterable<Transaction> inStoreTransactions) {
        // Find cutoff date and check transactions in store to match transactions in batch.

        Date cutoffDate = getCutoffDate(batchTransactions);

        if (cutoffDate == null) {
            return;
        }

        List<Transaction> transactionsToDelete = Lists.newArrayList();

        DuplicateTransactionDetector detector = new DuplicateTransactionDetector(batchTransactions);
        
        for (Transaction inStoreTrans : inStoreTransactions) {

            // Only look at transaction after the cutoff date.

            if (inStoreTrans.getOriginalDate().before(cutoffDate)) {
                continue;
            }

            // Try to find transaction in batchTransactions list.

            Optional<Transaction> detectedTransaction = detector.findAndRemoveDuplicate(inStoreTrans);

            if (!detectedTransaction.isPresent()) {
                transactionsToDelete.add(inStoreTrans);
            }
        }

        deleteFromDbAndIndex(transactionsToDelete);
    }

    private void deleteFromDbAndIndex(List<Transaction> transactionsToDelete) {
        if (transactionsToDelete.size() == 0) {
            return;
        }

        if (transactionsToDelete.size() > 1000) {
            log.warn(context.getUser().getId(), "Removing surprisingly many redundant transactions: " + transactionsToDelete.size());
        }

        indexedTransactionDAO.delete(transactionsToDelete);

        // Remove transactions from inStoreMap since this map is passed on to statistics worker.
        redundantTransactionsHistogram.update(transactionsToDelete.size());

        for (Transaction transaction : transactionsToDelete) {
            context.getTransactionsToDelete().add(transaction);
        }
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {
        // NOOP.
        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() {
        if (NON_APPLICABLE_PROVIDERS.contains(provider.getName())) {
            return;
        }

        if (context.getInBatchTransactions().size() == 0) {
            return;
        }

        // Have to handle Handelsbanken separately.

        boolean isHandelsbanken = false;

        if (Objects.equal("handelsbanken", provider.getName()) || Objects.equal("handelsbanken-bankid", provider.getName())) {
            isHandelsbanken = true;
        }

        // Sort transactions in this batch per account.

        ListMultimap<String, Transaction> thisBatchTransactionsPerAccountMap = Multimaps.index(
                context.getInBatchTransactions(), Transaction::getAccountId);

        // Go over all accounts and check for duplicates.

        for (final String accountId : thisBatchTransactionsPerAccountMap.keySet()) {
            // Get in-store transactions for account.

            Iterable<Transaction> inStoreTransactions = Iterables.filter(context.getUserData().getInStoreTransactions()
                    .values(), t -> t.getAccountId().equals(accountId));

            // Get in-batch transactions for account.

            List<Transaction> batchTransactions = Lists.newArrayList(thisBatchTransactionsPerAccountMap.get(accountId));

            // Need to look at transaction type and compare separately on Handelsbanken accounts.

            if (isHandelsbanken) {
                ImmutableListMultimap<TransactionTypes, Transaction> inBatchTransactionsByType = Multimaps.index(
                        batchTransactions, Transaction::getType);

                ImmutableListMultimap<TransactionTypes, Transaction> inStoreTransactionsByType = Multimaps.index(
                        inStoreTransactions, Transaction::getType);

                // Loop transaction types and check for duplicates.

                for (TransactionTypes type : inBatchTransactionsByType.keySet()) {
                    checkAndDeleteRedundantTransactions(inBatchTransactionsByType.get(type),
                            inStoreTransactionsByType.get(type));
                }
            } else {
                checkAndDeleteRedundantTransactions(batchTransactions, inStoreTransactions);
            }
        }
    }

    /**
     * Determine the cutoff date, that is the date from where the inStoreList and inBatchList should be compared from.
     * 
     * Take 5 days from oldest transaction.
     * 
     * @param transactions
     * @return
     */
    public Date getCutoffDate(Iterable<Transaction> transactions) {
        // Find the oldest transaction

        Transaction oldestTransaction = Ordering.from(Orderings.TRANSACTION_ORIGINAL_DATE_ORDERING).min(transactions);

        if (oldestTransaction == null) {
            return null;
        }

        // Get 5 days after oldest date.

        return DateUtils.addDays(oldestTransaction.getOriginalDate(), 5);
    }
}
