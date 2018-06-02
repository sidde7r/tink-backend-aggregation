package se.tink.backend.system.workers.processor.deduplication.detector;

import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.connector.rpc.PartnerTransactionPayload;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.system.workers.processor.deduplication.DeduplicationResult;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DeterministicPendingTransactionMatcher {

    private static final LogUtils log = new LogUtils(DeterministicPendingTransactionMatcher.class);

    private final ImmutableListMultimap<String, Transaction> pendingTransactionsInStoreByAccount;
    private final Collection<Transaction> inBatchTransactions;

    public DeterministicPendingTransactionMatcher(
            Collection<Transaction> inStoreTransactions,
            Collection<Transaction> inBatchTransactions) {

        pendingTransactionsInStoreByAccount = FluentIterable
                .from(inStoreTransactions)
                .filter(Predicates.filterTransactionsOnIsPending(true))
                .index(Transaction::getAccountId);
        this.inBatchTransactions = inBatchTransactions;
    }

    public boolean hasPendingTransactionsInStore() {
        return pendingTransactionsInStoreByAccount.size() > 0;
    }

    public boolean hasPendingTransactionsInStore(String accountId) {
        return pendingTransactionsInStoreByAccount.containsKey(accountId) && getPendingInStore(accountId).size() > 0;
    }

    public List<Transaction> getPendingInStore(String accountId) {
        return pendingTransactionsInStoreByAccount.get(accountId);
    }

    public DeduplicationResult matchAndUpdate() {
        ListMultimap<String, Transaction> nonPendingBatchTransactionsPerAccountMap = FluentIterable
                .from(inBatchTransactions)
                .filter(Predicates.filterTransactionsOnIsPending(false))
                .index(Transaction::getAccountId);

        // Goes through incoming (batch) non-pending transactions per account. If a transaction refers to other
        // pending transactions in PENDING_IDS, these are matched and the pending transactions are replaced with the
        // non-pending ones, but keeping user modified categories. Pending transactions older than a certain amount of
        // days are removed, just to be sure.

        List<Transaction> toUpdate = Lists.newArrayList();
        List<Transaction> toRemove = Lists.newArrayList();

        for (String accountId : nonPendingBatchTransactionsPerAccountMap.keySet()) {

            List<Transaction> pendingTransactions = getPendingInStore(accountId);

            if (pendingTransactions.isEmpty()) {
                continue;
            }

            for (final Transaction bookedTransaction : nonPendingBatchTransactionsPerAccountMap.get(accountId)) {

                List<Transaction> pendingTransactionMatches = matchAndUpdate(bookedTransaction);

                toUpdate.add(bookedTransaction);
                toRemove.addAll(pendingTransactionMatches);
            }
        }

        return new DeduplicationResult(Lists.newArrayList(), toUpdate, toRemove);
    }

    public List<Transaction> matchAndUpdate(Transaction nonPendingBatchTransaction) {

        PartnerTransactionPayload partnerPayload = getPartnerPayload(nonPendingBatchTransaction);
        if (partnerPayload.getPendingIds().isEmpty()) {
            return Lists.newArrayList();
        }

        Set<String> pendingIds = partnerPayload.getPendingIds().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Transaction> pendingInStore = getPendingInStore(nonPendingBatchTransaction.getAccountId());

        List<Transaction> matchedPendingTransactions = pendingInStore.stream().filter(pending -> pendingIds
                .contains(pending.getPayloadValue(TransactionPayloadTypes.EXTERNAL_ID)))
                .collect(Collectors.toList());

        updateBookedTransactionData(nonPendingBatchTransaction, matchedPendingTransactions);

        return matchedPendingTransactions;
    }

    private PartnerTransactionPayload getPartnerPayload(Transaction transaction) {
        String partnerPayload = transaction.getInternalPayload(Transaction.InternalPayloadKeys.PARTNER_PAYLOAD);
        if (!Strings.isNullOrEmpty(partnerPayload)) {
            return SerializationUtils.deserializeFromString(partnerPayload, PartnerTransactionPayload.class);
        }
        return new PartnerTransactionPayload();
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
                bookedTransaction.setCategory(
                        pendingWithUserModifiedCategory.get().getCategoryId(),
                        pendingWithUserModifiedCategory.get().getCategoryType());
                bookedTransaction.setUserModifiedCategory(true);
            }
        }
    }

    public List<Transaction> findExpiredPending(String accountId, List<Transaction> alreadyToRemove) {
        Date now = new Date();
        List<String> idsToRemove = alreadyToRemove.stream()
                .map(Transaction::getId)
                .collect(Collectors.toList());

        // Get all pending that did not match an incoming booked, and is older than expiration date.
        return getPendingInStore(accountId).stream()
                .filter(pending -> !idsToRemove.contains(pending.getId()))
                .filter(pending -> {
                    Date expiration = getPartnerPayload(pending).getPendingTransactionExpirationDate();
                    if (expiration == null) {
                        return false;
                    }
                    return now.after(expiration);
                })
                .peek(pending -> log
                        .info(pending.getUserId(), "Found expired pending transaction, removing: " + pending.getId()))
                .collect(Collectors.toList());
    }
}
