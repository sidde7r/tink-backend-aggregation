package se.tink.backend.system.workers.processor.transfers;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryChangeRecord;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.transfers.scoring.TransferDetectionScorer;
import se.tink.backend.system.workers.processor.transfers.scoring.TransferDetectionScorerFactory;
import se.tink.backend.utils.LogUtils;

/**
 * Matches transfers made within the same Tink account.
 * <p>
 * Sets:
 * categoryId
 * type
 * categoryType
 * TransactionPayloadTypes.TRANSFER_TWIN
 * TransactionPayloadTypes.TRANSFER_ACCOUNT
 */
public class TransferDetectionCommand implements TransactionProcessorCommand {

    private final static LogUtils log = new LogUtils(TransferDetectionCommand.class);

    private final Category unknownTransferCategory;
    private final Category unknownIncomeCategory;
    private final Category unknownExpenseCategory;
    private final TransferDetectionScorerFactory transferDetectionScorerFactory;
    private final TransactionProcessorContext context;
    private final CategoryChangeRecordDao categoryChangeRecordDao;

    private TransferDetectionScorer scorer;

    public TransferDetectionCommand(
            TransactionProcessorContext context,
            CategoryConfiguration categoryConfiguration, TransferDetectionScorerFactory transferDetectionScorerFactory,
            ClusterCategories categories,
            CategoryChangeRecordDao categoryChangeRecordDao
    ) {
        this.context = context;
        ImmutableMap<String, Category> categoriesByCode = Maps.uniqueIndex(categories.get(), Category::getCode);

        this.unknownTransferCategory = Preconditions.checkNotNull(
                categoriesByCode.get(categoryConfiguration.getTransferUnknownCode())
        );
        this.unknownIncomeCategory = Preconditions.checkNotNull(
                categoriesByCode.get(categoryConfiguration.getIncomeUnknownCode())
        );
        this.unknownExpenseCategory = Preconditions.checkNotNull(
                categoriesByCode.get(categoryConfiguration.getExpenseUnknownCode())
        );

        this.transferDetectionScorerFactory = transferDetectionScorerFactory;
        this.categoryChangeRecordDao = categoryChangeRecordDao;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("unknownTransferCategory", unknownTransferCategory)
                .add("unknownIncomeCategory", unknownIncomeCategory)
                .add("unknownExpenseCategory", unknownExpenseCategory)
                .toString();
    }

    @Override
    public TransactionProcessorCommandResult initialize() {
        scorer = transferDetectionScorerFactory.build(context.getUserData().getAccounts());
        return TransactionProcessorCommandResult.CONTINUE;
    }

    /**
     * Check if there are any big transfers ( > 50 000) in same account that are
     * transfers
     */
    @Override
    public void postProcess() {

        Iterable<Transaction> iterable = Iterables.filter(context.getInBatchTransactions(),
                input -> Math.abs(input.getAmount()) > 50000);

        Iterator<Transaction> outerIt = iterable.iterator();
        Iterator<Transaction> innerIt = iterable.iterator();

        while (outerIt.hasNext()) {
            Transaction outer = outerIt.next();
            while (innerIt.hasNext()) {
                Transaction inner = innerIt.next();
                if (outer.canBeTwinTransfer(inner)) {
                    updateTransactions(context, outer, inner);
                    log.info(context.getUser().getId(),
                            "Found large transfers on same account: " + outer.getOriginalDescription() + " --- "
                                    + inner.getOriginalDescription());
                    return;
                }
            }
        }
    }

    private HashMap<String, Transaction> getPotentialTwins(Transaction transaction,
            TransactionProcessorContext context) {
        HashMap<String, Transaction> potentialTwins = Maps.newHashMap();

        // Potential twins from store
        for (Transaction oldTransaction : context.getUserData().getInStoreTransactions().values()) {
            if (transaction.canBeTwinTransfer(oldTransaction)) {
                potentialTwins.put(oldTransaction.getId(), oldTransaction);
            }
        }

        // Potential twins from batch
        for (Transaction oldTransaction : context.getInBatchTransactions()) {
            if (transaction.canBeTwinTransfer(oldTransaction)) {
                potentialTwins.put(oldTransaction.getId(), oldTransaction);
            }
        }

        return potentialTwins;
    }

    /**
     * 1. Get saved transactions and the transaction in batch and check if any of the current transactions has a
     * corresponding twin transaction
     * 2. If match and
     * a) Transaction isn't matched against any other transaction => Update
     * b) Transaction score is better than old twin => Update
     * c) Transaction score is worse than old twin => Don't update
     */
    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {

        // Check if transactionType is set from agent or from an earlier transfer detection
        if (TransactionTypes.CREDIT_CARD.equals(transaction.getType())
                || CategoryTypes.TRANSFERS.equals(transaction.getCategoryType())) {
            return TransactionProcessorCommandResult.CONTINUE;
        }

        HashMap<String, Transaction> potentialTwins = getPotentialTwins(transaction, context);

        // Sort the potential twins by score in descending order
        TreeMultimap<Double, Transaction> twinsSortedByScoreDesc = scorePotentialTwins(transaction, potentialTwins);

        for (Map.Entry<Double, Transaction> entry : twinsSortedByScoreDesc.entries()) {

            Transaction potentialTwin = entry.getValue();
            Double score = entry.getKey();

            final String existingTwinId = potentialTwin.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN);

            if (Strings.isNullOrEmpty(existingTwinId)) {
                updateTransactions(context, potentialTwin, transaction);
                break;
            }

            // Check if the the current twin transaction is pointing on this transaction
            if (existingTwinId.equals(transaction.getId())) {
                break;
            }

            // If the transaction found already has a twin transfer marked, check if this twin is better than
            // then one found here, pick the best one.
            Transaction existingTwinTransaction = context.getTransaction(existingTwinId);

            // Check for pickedTransaction in store
            if (existingTwinTransaction == null) {
                existingTwinTransaction = context.getUserData().getInStoreTransaction(existingTwinId);
            }

            // We did not find the transaction so update with the twin (should not happen)
            if (existingTwinTransaction == null) {
                updateTransactions(context, potentialTwin, transaction);
                break;
            }

            double existingTwinScore = scorer.getScore(potentialTwin, existingTwinTransaction);

            if (score > existingTwinScore) {
                revertTransferPickedEarlier(context, transaction, existingTwinTransaction, potentialTwin);
                break;
            }
        }

        return TransactionProcessorCommandResult.CONTINUE;
    }

    private TreeMultimap<Double, Transaction> scorePotentialTwins(Transaction source, Map<String, Transaction> twins) {

        // Map with keys in descending order
        TreeMultimap<Double, Transaction> result = TreeMultimap.create(Ordering.natural().reverse(),
                Ordering.natural());

        for (Transaction twin : twins.values()) {
            result.put(scorer.getScore(source, twin), twin);
        }

        return result;
    }

    private void revertTransferPickedEarlier(TransactionProcessorContext context, Transaction transaction,
            Transaction pickedTransaction, Transaction bestTransaction) {
        // we have to update the old transaction and make the new transaction
        // the corresponding transfer
        String oldCategoryId = pickedTransaction.getCategoryId();

        pickedTransaction.setType(TransactionTypes.DEFAULT);
        pickedTransaction.resetCategory();
        pickedTransaction.removePayload(TransactionPayloadTypes.TRANSFER_TWIN);
        pickedTransaction.removePayload(TransactionPayloadTypes.TRANSFER_ACCOUNT);

        if (pickedTransaction.getOriginalAmount() > 0) {
            pickedTransaction.setCategory(unknownIncomeCategory);
        } else {
            pickedTransaction.setCategory(unknownExpenseCategory);
        }

        reportCategoryChangeRecord(context, oldCategoryId, pickedTransaction);
        context.addTransactionToUpdateListPresentInDb(pickedTransaction.getId());
        updateTransactions(context, bestTransaction, transaction);
    }

    private void updateTransactions(TransactionProcessorContext context, Transaction oldTransaction,
            Transaction newTransaction) {

        // save a change record for the old transaction, need the category
        // before changing
        String oldCategory = oldTransaction.getCategoryId();

        // Update old transactions
        oldTransaction.setType(TransactionTypes.TRANSFER);
        if (!oldTransaction.isUserModifiedCategory()) {
            oldTransaction.setCategory(unknownTransferCategory);
        }
        oldTransaction.setPayload(TransactionPayloadTypes.TRANSFER_TWIN, newTransaction.getId());
        oldTransaction.setPayload(TransactionPayloadTypes.TRANSFER_ACCOUNT, newTransaction.getAccountId());
        context.addTransactionToUpdateListPresentInDb(oldTransaction.getId());

        // Update new transactions
        newTransaction.setType(TransactionTypes.TRANSFER);
        if (!newTransaction.isUserModifiedCategory()) {
            newTransaction.setCategory(unknownTransferCategory);
        }
        newTransaction.setPayload(TransactionPayloadTypes.TRANSFER_TWIN, oldTransaction.getId());
        newTransaction.setPayload(TransactionPayloadTypes.TRANSFER_ACCOUNT, oldTransaction.getAccountId());

        reportCategoryChangeRecord(context, oldCategory, oldTransaction);
        logTransferFound(newTransaction, oldTransaction);
    }

    private void logTransferFound(Transaction t, Transaction o) {
        log.debug(t.getUserId(), t.getCredentialsId(),
                "Found two transfers that match: " + "thisTrans: " + t.getDescription() + " " + t.getOriginalDate()
                        + " " + t.getOriginalAmount() + " ---- oldTrans: " + o.getDescription() + " "
                        + o.getOriginalDate() + " " + o.getOriginalAmount());
    }

    private void reportCategoryChangeRecord(TransactionProcessorContext context, String oldCategoryId,
            Transaction transaction) {
        categoryChangeRecordDao.save(CategoryChangeRecord.createChangeRecord(transaction,
                Optional.ofNullable(oldCategoryId), this.toString()),
                categoryChangeRecordDao.CATEGORY_CHANGE_RECORD_TTL_DAYS, TimeUnit.DAYS);
    }
}
