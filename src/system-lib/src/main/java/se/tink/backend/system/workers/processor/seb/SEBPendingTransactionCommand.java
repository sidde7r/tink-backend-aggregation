package se.tink.backend.system.workers.processor.seb;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.connector.rpc.seb.PartnerTransactionPayload;
import se.tink.backend.core.CategoryChangeRecord;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.utils.PendingTransactionHelper;
import se.tink.backend.utils.guavaimpl.Orderings;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SEBPendingTransactionCommand implements TransactionProcessorCommand {

    private static final LogUtils log = new LogUtils(SEBPendingTransactionCommand.class);
    private ImmutableListMultimap<String, Transaction> pendingTransactionsInStoreByAccount;
    private CategoryChangeRecordDao categoryChangeRecordDao;

    private final TransactionProcessorContext context;

    public SEBPendingTransactionCommand(TransactionProcessorContext context,
            CategoryChangeRecordDao categoryChangeRecordDao) {
        this.context = context;
        this.categoryChangeRecordDao = categoryChangeRecordDao;
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {
        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public TransactionProcessorCommandResult initialize() {
        pendingTransactionsInStoreByAccount = FluentIterable
                .from(context.getUserData().getInStoreTransactions().values())
                .filter(Predicates.filterTransactionsOnIsPending(true))
                .index(Transaction::getAccountId);

        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() {
        if (pendingTransactionsInStoreByAccount.size() == 0) {
            return;
        }

        ListMultimap<String, Transaction> nonPendingBatchTransactionsPerAccount = FluentIterable
                .from(context.getInBatchTransactions())
                .filter(Predicates.filterTransactionsOnIsPending(false))
                .index(Transaction::getAccountId);

        // Go through incoming (batch) booked transactions per account and check:
        //      1. For a match using reservationId with a pending transaction in our db (store)
        //      2. For a match using date, amount and description
        //      3. Remove the pending if older than the expiration date on payload.

        for (String accountId : nonPendingBatchTransactionsPerAccount.keySet()) {
            matchAccount(context, nonPendingBatchTransactionsPerAccount, accountId);
        }
    }

    private void matchAccount(
            TransactionProcessorContext context,
            ListMultimap<String, Transaction> nonPendingBatchTransactionsPerAccount,
            String accountId) {

        List<Transaction> pendingTransactions = pendingTransactionsInStoreByAccount.get(accountId).stream()
                .sorted(Orderings.TRANSACTION_DATE_ORDERING).collect(Collectors.toList());
        if (pendingTransactions.isEmpty()) {
            return;
        }

        Set<String> alreadyMatchedPendingTransactionIds = Sets.newHashSet();

        for (final Transaction bookedTransaction : nonPendingBatchTransactionsPerAccount.get(accountId)) {
            matchBookedTransaction(context, pendingTransactions, alreadyMatchedPendingTransactionIds,
                    bookedTransaction);
        }

        // Remove all pending that did not match an incoming booked, and is older than expiration date.
        Date now = new Date();
        pendingTransactions.stream()
                .filter(pending -> !alreadyMatchedPendingTransactionIds.contains(pending.getId()))
                .filter(pending -> {
                    Date expiration = getSEBPayload(pending).getPendingTransactionExpirationDate();
                    if (expiration == null) {
                        return false;
                    }
                    return now.after(expiration);
                })
                .peek(pending -> log
                        .info(pending.getUserId(), "Found expired pending transaction, removing: " + pending.getId()))
                .forEach(context::addTransactionToDelete);
    }

    private void matchBookedTransaction(TransactionProcessorContext context, List<Transaction> pendingTransactions,
            Set<String> alreadyMatchedPendingTransactionIds, Transaction bookedTransaction) {
        PartnerTransactionPayload sebPayload = getSEBPayload(bookedTransaction);

        if (sebPayload.getReservationIds() != null && !sebPayload.getReservationIds().isEmpty()) {

            // SEB has set multiple reservation ids, corresponding to multiple pending transactions.
            matchMultipleReservations(context, pendingTransactions, alreadyMatchedPendingTransactionIds,
                    bookedTransaction,
                    sebPayload);

        } else if (!Strings.isNullOrEmpty(sebPayload.getReservationId())) {
            // Legacy, remove this else-if clause SEB has deployed the removal of "RESERVATION_ID"

            // SEB has set a single reservation id, corresponding to a single pending transaction.
            matchSingleReservation(context, pendingTransactions, alreadyMatchedPendingTransactionIds, bookedTransaction,
                    sebPayload);

        } else {
            // SEB has not set any reservation id, so fuzzy matching must be done. However, this should
            // probably never be the case.
            matchLegacyUsingFuzzyMatching(context, pendingTransactions, alreadyMatchedPendingTransactionIds,
                    bookedTransaction);
        }
    }

    private void matchLegacyUsingFuzzyMatching(TransactionProcessorContext context,
            List<Transaction> pendingTransactions, Set<String> alreadyMatchedPendingTransactionIds,
            Transaction bookedTransaction) {

        List<Transaction> fuzzyMatchedPending = pendingTransactions.stream()
                .filter(pending -> fuzzyMatch(pending, bookedTransaction))
                .filter(pending -> !alreadyMatchedPendingTransactionIds.contains(pending.getId()))
                .collect(Collectors.toList());

        // Even if we match multiple pending, we don't want to delete them all. We only take the first best.
        if (fuzzyMatchedPending.size() > 0) {
            updateTransactions(context, fuzzyMatchedPending.get(0), bookedTransaction);
            alreadyMatchedPendingTransactionIds.add(fuzzyMatchedPending.get(0).getId());
            log.info(context.getUser().getId(), "Found pending transaction by heuristics");
        }
    }

    private void matchSingleReservation(TransactionProcessorContext context, List<Transaction> pendingTransactions,
            Set<String> alreadyMatchedPendingTransactionIds, Transaction bookedTransaction,
            PartnerTransactionPayload sebPayload) {

        String reservationId = sebPayload.getReservationId();

        List<Transaction> matchedPendingTransactions = pendingTransactions.stream()
                .filter(pending -> Objects.equals(getExternalIdFromPendingTransaction(pending), reservationId))
                .collect(Collectors.toList());

        for (Transaction pending : matchedPendingTransactions) {
            updateTransactions(context, pending, bookedTransaction);
            alreadyMatchedPendingTransactionIds.add(pending.getId());
            log.info(context.getUser().getId(), "Found pending transaction by reservationId");
        }
    }

    private void matchMultipleReservations(TransactionProcessorContext context, List<Transaction> pendingTransactions,
            Set<String> alreadyMatchedPendingTransactionIds, Transaction bookedTransaction,
            PartnerTransactionPayload sebPayload) {

        Set<String> reservationIds = sebPayload.getReservationIds().stream()
                .filter(Objects::nonNull) // just make sure
                .collect(Collectors.toSet());

        List<Transaction> matchedPendingTransactions = pendingTransactions.stream()
                .filter(pending -> reservationIds.contains(getExternalIdFromPendingTransaction(pending)))
                .collect(Collectors.toList());

        log.info(context.getUser().getId(),
                String.format("Found %s pending transactions from booked with multiple reservationIds",
                        matchedPendingTransactions.size()));

        updateTransactions(context, matchedPendingTransactions, bookedTransaction);
        alreadyMatchedPendingTransactionIds
                .addAll(matchedPendingTransactions.stream().map(Transaction::getId)
                        .collect(Collectors.toList()));
    }

    private PartnerTransactionPayload getSEBPayload(Transaction transaction) {
        String sebPayload = transaction.getInternalPayload(Transaction.InternalPayloadKeys.SEB_PAYLOAD);
        if (!Strings.isNullOrEmpty(sebPayload)) {
            PartnerTransactionPayload payload = SerializationUtils
                    .deserializeFromString(sebPayload, PartnerTransactionPayload.class);

            if (payload == null) {
                log.info(transaction.getUserId(), "Received an invalid payload");
                return new PartnerTransactionPayload();
            }
            return payload;
        }
        return new PartnerTransactionPayload();
    }

    private boolean fuzzyMatch(Transaction pendingTransaction, Transaction batchTransaction) {
        return PendingTransactionHelper.isOldPendingTransaction(pendingTransaction, batchTransaction);
    }

    /**
     * Updates a booked transaction with data from multiple pending, and deletes all pending. The booked transaction
     * gets the category from the pending transaction with highest amount.
     * <p>
     * NOTE! Not saving the old pending transaction, but rather move over categoryId.
     * This since SEB gets a new ID on their side and hence we cannot keep the old one
     * as we do in SE cluster.
     */
    private void updateTransactions(TransactionProcessorContext context, List<Transaction> pendingTransactions,
            Transaction bookedTransaction) {

        updateBookedTransactionData(bookedTransaction, pendingTransactions);

        context.addTransactionToUpdateListPresentInDb(bookedTransaction.getId());
        for (Transaction pendingTransaction : pendingTransactions) {
            context.addTransactionToDelete(pendingTransaction);
        }
    }

    /**
     * Updates a booked transaction with data from a pending, and deletes the pending.
     * <p>
     * NOTE! Not saving the old pending transaction, but rather move over categoryId.
     * This since SEB gets a new ID on their side and hence we cannot keep the old one
     * as we do in SE cluster.
     */
    private void updateTransactions(TransactionProcessorContext context, Transaction pendingTransaction,
            Transaction bookedTransaction) {

        updateBookedTransactionData(bookedTransaction, pendingTransaction);

        context.addTransactionToDelete(pendingTransaction);
        context.addTransactionToUpdateListPresentInDb(bookedTransaction.getId());
    }

    private String getExternalIdFromPendingTransaction(Transaction transaction) {
        PartnerTransactionPayload payload = getSEBPayload(transaction);
        if (payload != null && !Strings.isNullOrEmpty(payload.getReservationId())) {
            // Legacy, remove this when SEB has deployed the removal of "RESERVATION_ID"
            return payload.getReservationId();
        }
        return transaction.getPayloadValue(TransactionPayloadTypes.EXTERNAL_ID);
    }

    private void updateBookedTransactionData(Transaction bookedTransaction, List<Transaction> pendingTransactions) {
        bookedTransaction.setPayload(TransactionPayloadTypes.UNSETTLED_AMOUNT,
                String.valueOf(pendingTransactions.stream().mapToDouble(Transaction::getAmount).sum()));

        Optional<Transaction> pendingWithUserModifiedCategory = pendingTransactions.stream()
                .filter(t -> t.getCategoryId() != null)
                .filter(Transaction::isUserModifiedCategory)
                .max(Comparator.comparingDouble(t -> Math.abs(t.getAmount())));

        if (pendingWithUserModifiedCategory.isPresent()) {
            if (pendingWithUserModifiedCategory.get().getAmount() / bookedTransaction.getAmount() > 0.5) {
                Optional<String> oldCategory = Optional.ofNullable(bookedTransaction.getCategoryId());

                bookedTransaction.setCategory(
                        pendingWithUserModifiedCategory.get().getCategoryId(),
                        pendingWithUserModifiedCategory.get().getCategoryType());
                bookedTransaction.setUserModifiedCategory(true);

                categoryChangeRecordDao.save(CategoryChangeRecord.createChangeRecord(bookedTransaction,
                        oldCategory, this.toString()), CategoryChangeRecordDao.CATEGORY_CHANGE_RECORD_TTL_DAYS,
                        TimeUnit.DAYS);
            }
        }
    }

    private void updateBookedTransactionData(Transaction bookedTransaction, Transaction pendingTransaction) {
        bookedTransaction.setPayload(TransactionPayloadTypes.UNSETTLED_AMOUNT,
                String.valueOf(pendingTransaction.getOriginalAmount()));

        if (pendingTransaction.isUserModifiedCategory() && pendingTransaction.getCategoryId() != null) {
            Optional<String> oldCategory = Optional.ofNullable(bookedTransaction.getCategoryId());

            bookedTransaction.setCategory(pendingTransaction.getCategoryId(), pendingTransaction.getCategoryType());
            bookedTransaction.setUserModifiedCategory(true);

            categoryChangeRecordDao.save(CategoryChangeRecord.createChangeRecord(bookedTransaction,
                    oldCategory, this.toString()), CategoryChangeRecordDao.CATEGORY_CHANGE_RECORD_TTL_DAYS,
                    TimeUnit.DAYS);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }
}
